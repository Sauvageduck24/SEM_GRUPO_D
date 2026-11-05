# Propuesta local P2.2

Esta rama prepara una referencia para revisar. No es una versión acordada ni fusionada del equipo. Base remota leída el 4 de noviembre de 2026: 21639e411b47adff40d07cc715d1cfe74c1742cb. Se conserva README y documentación P2.1.

Se incorporan los fuentes docentes, con RegistroSalud, RegistroSaludEntidad, RegistroSaludDao y RespuestaRegistrosSalud. El contrato JSON conserva elementos, id, titulo, contenido y fecha para mantener compatibilidad con el backend común. El cierre sigue la corrección 5.6 de P2.2. Esta propuesta no añade las pantallas de P2.3 ni la cámara o la API de medicamentos del alcance futuro.

El reparto que YA consta en README de main asigna sesión a Ángel, almacenamiento a David, API a Jorge y cámara a Esteban. Se reproduce como antecedente documentado, no como un nuevo acuerdo para P2.2. El grupo debe validar la referencia, repartir la integración y revisar la rama antes de incorporarla a main.

La aplicación sigue deliberadamente insegura (HTTP, BODY, preferencias y Room en claro). Solo usar datos ficticios de laboratorio. No contiene claves privadas ni credenciales personales. Las credenciales de ejemplo del backend son material docente, no secretos del equipo.
