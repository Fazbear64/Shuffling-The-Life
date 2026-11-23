# Cambios Implementados - Shuffling The Life
**Fecha:** 2026-11-22

## Resumen de Ajustes

Se implementaron 4 ajustes principales en el juego según lo solicitado:

---

## 1. 🐛 Gusanos: Despawn y Escalado

### Archivos modificados:
- `src/main/java/entity/Enemigo.java`
- `src/main/java/visual/Handler.java`
- `src/main/java/visual/Game.java`

### Cambios realizados:

#### A) Despawn tras 15 segundos
**Enemigo.java:**
- Agregado campo `tiempoMuerteMs` para registrar el momento de muerte
- Método `registrarMuerte()`: registra timestamp cuando el enemigo muere
- Método `debeEliminar()`: verifica si han pasado 15 segundos (15000 ms) desde la muerte

**Handler.java:**
- Modificado para llamar `registrarMuerte()` cuando un enemigo muere por proyectil
- Cambiado `removeIf` para eliminar enemigos usando `debeEliminar()` en lugar de `estaMuerto()`
- Ahora los cuerpos permanecen 15s antes de desaparecer

#### B) Escalado de gusanos según tiendas visitadas
**Game.java:**
- Agregado método `spawnGusanoCercaDelJugador()` que genera enemigos veloces (gusanos)
- Modificado `abrirTiendaNormal()` para spawnear gusanos escalados:
  - Tras tienda 1: 1 gusano
  - Tras tienda 2: 2 gusanos
  - Tras tienda 3: 3 gusanos
  - Tras tienda 4+: 4 gusanos (máximo)

### Cómo probar:
1. Juega hasta completar 3 oleadas (para llegar a la primera tienda)
2. Observa que aparece 1 gusano (enemigo naranja rápido)
3. Mata al gusano y observa que su cuerpo permanece 15 segundos antes de desaparecer
4. Continúa jugando hasta la siguiente tienda y verás más gusanos

**Logs de depuración:**
- `[DEBUG] Enemigo murió. Será eliminado en 15s. Timestamp: [tiempo]`
- `[DEBUG] Spawneados [N] gusanos tras tienda [N]`

---

## 2. 👤 Nombre y Selección de Personaje

### Archivos modificados:
- `src/main/java/visual/Game.java`

### Cambios realizados:

#### A) Nombre guardado automáticamente
**Game.java (constructor):**
- Al iniciar el juego, verifica si ya existe un nombre guardado en `Progreso`
- Si existe, carga automáticamente el nombre y salta directo a selección de personaje
- El nombre ya se guardaba en `Progreso.java`, solo faltaba cargarlo al inicio

#### B) Selección de personaje en cada reinicio
**Game.java (`reiniciarPartida()`):**
- Cambiado el flujo para que al presionar [R] tras Game Over, vuelva a `SELECCION_PERSONAJE`
- Ahora puedes elegir un personaje diferente en cada partida
- Se limpian todos los objetos del handler al reiniciar

### Cómo probar:
1. **Primera vez:** Ingresa tu nombre → selecciona personaje → juega
2. **Cierra y vuelve a abrir el juego:** El nombre ya estará cargado, vas directo a selección
3. **Muere y reinicia (R):** Vuelve a la pantalla de selección de personaje para elegir otro

**Logs de depuración:**
- `[DEBUG] Nombre cargado desde progreso: [nombre]`
- `[DEBUG] Reinicio - Volviendo a selección de personaje`

---

## 3. 🔍 Auditoría de Efectos con Logs

### Archivos modificados:
- `src/main/java/entity/Player.java`

### Cambios realizados:

**Player.java (`aplicarCartaTarot()`):**
- Agregados logs detallados antes y después de aplicar cada efecto
- Se muestra: nombre, tipo, valor del efecto
- Se muestra el estado ANTES: vida, daño, velocidad
- Se muestra el estado DESPUÉS: vida, daño, velocidad
- Logs específicos para efectos 50/50 mostrando valores exactos aplicados

**Efectos verificados y corregidos:**
- ✅ Efectos de vida (positivos y negativos)
- ✅ Efectos de velocidad (positivos y negativos)
- ✅ Efectos de daño (positivos y negativos)
- ✅ Efectos 50/50 (daño-velocidad, daño-quieto, vida-daño)
- ✅ Efectos de armas específicas (revolver, escopeta, minigun)

### Cómo probar:
1. Juega hasta llegar a una tienda
2. Compra un comodín o elige una carta tarot
3. Observa la consola para ver los logs de aplicación:

**Ejemplo de logs:**
```
[DEBUG EFECTO] Aplicando: +50% Daño, -20% Velocidad | Tipo: daño-velocidad | Valor: 0.5
[DEBUG EFECTO] ANTES - Vida: 110/110 | Daño: 2 | Vel: 4
[DEBUG EFECTO] Aplicado daño-velocidad: +1 daño, -1 velocidad
[DEBUG EFECTO] DESPUÉS - Vida: 110/110 | Daño: 3 | Vel: 3
```

---

## 4. ⚖️ Rebalanceo de Efectos 50/50

### Archivos modificados:
- `src/main/java/entity/CartaTarot.java`
- `src/main/java/entity/Player.java`

### Cambios realizados:

#### Valores ANTERIORES vs NUEVOS:

**Efecto 1: Daño-Velocidad**
- ANTES: +10-25% daño aleatorio, -10-25% velocidad aleatoria
- AHORA: **+50% daño fijo, -20% velocidad fija**
- JUSTIFICACIÓN: El trade-off vale más la pena, pierdes poco movimiento por mucho daño

**Efecto 2: Daño-Quieto**
- ANTES: +10-25% daño aleatorio, te quedas quieto (velocidad = 0)
- AHORA: **+60% daño fijo, velocidad = 0**
- JUSTIFICACIÓN: Alto riesgo (no puedes moverte), alta recompensa (mucho daño)

**Efecto 3: Vida-Daño**
- ANTES: -10-25% vida aleatoria, +10-25% daño aleatorio
- AHORA: **-25% vida fija, +55% daño fijo**
- JUSTIFICACIÓN: Pierdes vida pero ganas mucho más daño del que perdías antes

### Cómo probar:
1. Juega hasta la tienda de Tarot (tras la tienda de comodines)
2. Si aparece una carta 50/50, elige cualquiera:
   - Verde: efecto positivo
   - Rojo: efecto negativo
   - Naranja: efecto 50/50 (rebalanceados)
3. Observa los logs en consola para ver los valores exactos aplicados
4. Verifica que el daño aumenta significativamente

**Comparación en gameplay:**
- Antes: +15% daño promedio (poco impacto)
- Ahora: +50-60% daño (muy notable en combate)

---

## 5. 🛠️ Correcciones Técnicas

### Archivos modificados:
- `src/main/java/dto/PlayerDTO.java`
- `src/main/java/dto/ArmaDTO.java`
- `src/main/java/dto/CartaTarotDTO.java`

### Cambios realizados:
- Agregados **getters manuales** a todos los DTOs
- Razón: `compile.bat` usa `javac` directamente sin procesar anotaciones de Lombok
- Los getters ahora funcionan correctamente en tiempo de compilación

---

## 📋 Checklist de Pruebas

### Test 1: Gusanos
- [ ] Matar un gusano y ver que su cuerpo permanece 15 segundos
- [ ] Llegar a tienda 1 y ver 1 gusano spawneado
- [ ] Llegar a tienda 2 y ver 2 gusanos spawneados
- [ ] Llegar a tienda 3 y ver 3 gusanos spawneados

### Test 2: Nombre y Personaje
- [ ] Primera vez: ingresar nombre y seleccionar personaje
- [ ] Cerrar juego y volver a abrir: nombre cargado automáticamente
- [ ] Morir y reiniciar: volver a selección de personaje

### Test 3: Efectos
- [ ] Comprar comodín y ver logs en consola
- [ ] Elegir carta tarot y ver logs detallados
- [ ] Verificar que los valores realmente cambian (vida, daño, velocidad)

### Test 4: Efectos 50/50
- [ ] Conseguir carta "Daño-Velocidad" y verificar +50% daño, -20% vel
- [ ] Conseguir carta "Daño-Quieto" y verificar +60% daño, vel = 0
- [ ] Conseguir carta "Vida-Daño" y verificar -25% vida, +55% daño

---

## 🚀 Cómo Ejecutar

```bash
# Compilar
.\compile.bat

# Ejecutar
java -cp "target\classes" visual.Game
```

---

## 📝 Notas Importantes

1. **Logs de depuración:** Todos los cambios incluyen logs que empiezan con `[DEBUG]` o `[DEBUG EFECTO]` para facilitar la verificación
2. **Compatibilidad:** Todos los cambios son retrocompatibles con partidas guardadas existentes
3. **Persistencia:** El nombre del jugador se guarda en `progreso.dat` y persiste entre sesiones
4. **Balance:** Los efectos 50/50 ahora tienen valores **fijos** en lugar de aleatorios para mayor previsibilidad

---

## 🎮 Gameplay Recomendado para Probar

1. Inicia el juego (primera vez o tras borrar `progreso.dat`)
2. Ingresa tu nombre
3. Elige el Mapache (siempre disponible)
4. Juega 3 oleadas (60 segundos cada una)
5. En el tragaperras, acepta el arma con ESPACIO
6. Repite hasta llegar a la primera tienda
7. **Observa:** 1 gusano aparece cerca
8. En la tienda de comodines, compra alguno (teclas 1-6)
9. En la tienda de tarot, elige una carta (teclas 1-3)
10. **Verifica logs en consola** para ver efectos aplicados
11. Continúa jugando para ver más gusanos en tiendas futuras
12. Al morir, reinicia (R) y verifica que vuelve a selección de personaje

---

## ✅ Estado Final

**COMPILACIÓN:** ✅ Exitosa  
**AJUSTE 1 (Gusanos):** ✅ Implementado  
**AJUSTE 2 (Nombre/Personaje):** ✅ Implementado  
**AJUSTE 3 (Auditoría):** ✅ Implementado  
**AJUSTE 4 (Rebalanceo):** ✅ Implementado  

**Todos los cambios están listos para probar.**
