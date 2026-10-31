# Legit Client (Fabric 1.21.4)

Красивый клиентский мод: анимированный ClickGUI, HUD, Zoom, Fullbright, Freelook, ToggleSprint, ToggleSneak.
Никаких функций с нечестным преимуществом (нет килауры, ESP, X-Ray и т.п.).

## Сборка
Нужны JDK 21, Gradle 8.12+ и интернет. (Либо скопируйте `gradlew`, `gradlew.bat` и папку `gradle/`
из https://github.com/FabricMC/fabric-example-mod и запускайте `./gradlew build`.)

    gradle build           # нужен Gradle 8.12+

Готовый мод: `build/libs/legitclient-1.0.0.jar` (не `-sources`).
Положите его в папку `mods` вместе с Fabric Loader и Fabric API для 1.21.4.

## Управление
| Клавиша      | Действие                          |
|--------------|-----------------------------------|
| Right Shift  | Открыть / закрыть меню            |
| C (держать)  | Zoom, колёсико меняет кратность   |
| Left Alt     | Freelook (держать)                |

В меню: ЛКМ по модулю - включить, ПКМ - настройки (слайдеры, переключатели).
Все клавиши меняются в настройках управления. Конфиг: `config/legitclient.json`.

## Другая версия Minecraft
Поменяйте значения в `gradle.properties` (https://fabricmc.net/develop). Для разных версий могут
отличаться сигнатуры в миксинах (`GameRendererMixin`, `CameraMixin`) и в GUI (`DrawContext`).

## Важно
Правила серверов разные: Freelook и Fullbright на некоторых серверах запрещены. Проверяйте правила.
