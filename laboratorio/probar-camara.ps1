param(
    [string]$Serie = 'emulator-5554',
    [string]$Salida = 'docs/p31/evidencias-esteban',
    [switch]$SinCompilar
)
$ErrorActionPreference = 'Stop'
$raizGrupo = Split-Path $PSScriptRoot -Parent
$sdkGrupo = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adbGrupo = Join-Path $sdkGrupo 'platform-tools\adb.exe'
$paqueteGrupo = 'es.uma.seguridad.salud'
$claseGrupo = "$paqueteGrupo.PermisoCamaraInformesTest"
$destinoGrupo = Join-Path (Join-Path $raizGrupo $Salida) (Get-Date -Format 'yyyyMMdd-HHmmss')
New-Item -ItemType Directory -Force -Path $destinoGrupo | Out-Null
function AdbGrupo([string[]]$Argumentos) {
    $respuestaGrupo = & $adbGrupo -s $Serie @Argumentos 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($respuestaGrupo | Out-String) }
    return ($respuestaGrupo | Out-String).Trim()
}
Push-Location $raizGrupo
try {
    if ((AdbGrupo -Argumentos @('shell','dumpsys','user')) -notmatch '0=RUNNING_UNLOCKED') {
        throw 'Desbloquea el emulador o dispositivo antes de ejecutar las pruebas (almacenamiento del usuario 0 bloqueado).'
    }
    if (!$SinCompilar) {
        if (!$env:JAVA_HOME) { $env:JAVA_HOME = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr' }
        & .\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest --console=plain *> (Join-Path $destinoGrupo 'compilacion.txt')
        if ($LASTEXITCODE -ne 0) { throw "Gradle falló; consulta $destinoGrupo\compilacion.txt" }
    }
    $paquetesGrupo = AdbGrupo -Argumentos @('shell','pm','list','packages',$paqueteGrupo)
    $anteriorGrupo = if (($paquetesGrupo -split '\r?\n') -contains "package:$paqueteGrupo") {
        AdbGrupo -Argumentos @('shell','dumpsys','package',$paqueteGrupo)
    } else { '' }
    $lineaPermisoGrupo = ($anteriorGrupo -split "`n" | Where-Object { $_ -match 'android.permission.CAMERA: granted=' } | Select-Object -Last 1)
    $concedidoAntesGrupo = $lineaPermisoGrupo -match 'granted=true'
    $userSetAntesGrupo = $lineaPermisoGrupo -match '\bUSER_SET\b'
    $userFixedAntesGrupo = $lineaPermisoGrupo -match '\bUSER_FIXED\b'
    $apkGrupo = Join-Path $raizGrupo 'app/build/outputs/apk/debug/app-debug.apk'
    $apkTestGrupo = Join-Path $raizGrupo 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
    AdbGrupo -Argumentos @('install','-r',$apkGrupo) | Write-Output
    AdbGrupo -Argumentos @('install','-r',$apkTestGrupo) | Write-Output
    try {
        # Sólo modificar CAMERA de SaludApp, nunca resetear permisos de las demás apps.
        AdbGrupo -Argumentos @('shell','pm','revoke',$paqueteGrupo,'android.permission.CAMERA') | Out-Null
        AdbGrupo -Argumentos @('shell','pm','clear-permission-flags',$paqueteGrupo,'android.permission.CAMERA','user-set','user-fixed') | Out-Null
        $suiteGrupo = AdbGrupo -Argumentos @('shell','am','instrument','-w','-r','-e','class',$claseGrupo,
            "$paqueteGrupo.test/androidx.test.runner.AndroidJUnitRunner")
        $suiteGrupo | Set-Content -LiteralPath (Join-Path $destinoGrupo 'instrumentacion.txt') -Encoding utf8
        if ($suiteGrupo -notmatch 'OK \(3 tests\)') { throw "La suite de cámara no pasó; consulta $destinoGrupo" }
        AdbGrupo -Argumentos @('pull',"/sdcard/Android/data/$paqueteGrupo/files/pruebas-camara",(Join-Path $destinoGrupo 'estados')) | Out-Null

        # Revocar fuera del runner: Android puede terminar el proceso al retirar CAMERA.
        AdbGrupo -Argumentos @('shell','pm','revoke',$paqueteGrupo,'android.permission.CAMERA') | Out-Null
        $revocacionGrupo = AdbGrupo -Argumentos @('shell','am','instrument','-w','-r','-e','class',
            "$claseGrupo#a_permisoDenegadoPermiteListado","$paqueteGrupo.test/androidx.test.runner.AndroidJUnitRunner")
        $revocacionGrupo | Set-Content -LiteralPath (Join-Path $destinoGrupo 'revocacion.txt') -Encoding utf8
        if ($revocacionGrupo -notmatch 'OK \(1 test\)') { throw 'Falló la prueba tras revocar CAMERA.' }
        AdbGrupo -Argumentos @('pull',"/sdcard/Android/data/$paqueteGrupo/files/pruebas-camara/01-sin-permiso-listado-utilizable.txt",
            (Join-Path $destinoGrupo 'estado-tras-revocar.txt')) | Out-Null
        AdbGrupo -Argumentos @('logcat','-d','-v','time','-s','EstebanCamara:I') |
            Set-Content -LiteralPath (Join-Path $destinoGrupo 'eventos-camara.txt') -Encoding utf8
        [ordered]@{
            fecha = (Get-Date).ToString('o'); rama = (git branch --show-current)
            base = (git rev-parse HEAD); paquete = $paqueteGrupo
            android = (AdbGrupo -Argumentos @('shell','getprop','ro.build.version.release'))
            sdk = (AdbGrupo -Argumentos @('shell','getprop','ro.build.version.sdk'))
            apk_sha256 = (Get-FileHash -LiteralPath $apkGrupo -Algorithm SHA256).Hash
            pruebas = '3 pruebas instrumentadas y 1 repetida después de revocar CAMERA'
            alcance = 'MainActivity con listado ficticio local; sin backend, sin login, sin captura de informes'
            sin_camara = 'Simulado mediante el parámetro del componente; el emulador sí tiene cámara'
            permiso_original_concedido = [bool]$concedidoAntesGrupo
        } | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $destinoGrupo 'resultado.json') -Encoding utf8
        Write-Output "Pruebas correctas. Evidencias: $destinoGrupo"
    } finally {
        if ($concedidoAntesGrupo) {
            AdbGrupo -Argumentos @('shell','pm','grant',$paqueteGrupo,'android.permission.CAMERA') | Out-Null
        } else {
            AdbGrupo -Argumentos @('shell','pm','revoke',$paqueteGrupo,'android.permission.CAMERA') | Out-Null
        }
        AdbGrupo -Argumentos @('shell','pm','clear-permission-flags',$paqueteGrupo,'android.permission.CAMERA','user-set','user-fixed') | Out-Null
        if ($userSetAntesGrupo) { AdbGrupo -Argumentos @('shell','pm','set-permission-flags',$paqueteGrupo,'android.permission.CAMERA','user-set') | Out-Null }
        if ($userFixedAntesGrupo) { AdbGrupo -Argumentos @('shell','pm','set-permission-flags',$paqueteGrupo,'android.permission.CAMERA','user-fixed') | Out-Null }
    }
} finally { Pop-Location }
