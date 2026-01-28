# Fractal Flame

Генератор фрактального пламени на Java с поддержкой многопоточности.

## Описание

Программа реализует алгоритм [Chaos Game](https://en.wikipedia.org/wiki/Chaos_game) для генерации фрактальных изображений типа "пламя" (fractal flames). Поддерживает множество нелинейных трансформаций и гибкую настройку через CLI-аргументы или JSON-конфигурацию.

## Возможности

- Генерация фрактальных изображений с настраиваемым разрешением
- Поддержка 6 вариаций трансформаций: LINEAR, SPHERICAL, SWIRL, HORSESHOE, EXPONENTIAL, SINUSOIDAL
- Многопоточная обработка для ускорения генерации
- Воспроизводимые результаты через seed
- Конфигурация через CLI-аргументы или JSON-файл
- Логарифмическое сглаживание яркости

## Требования

- Java 24
- Maven 3.9.11

## Сборка

```bash
mvn clean package
```

## Использование

### Базовый запуск

```bash
java -jar fractal-flame.jar
```

### CLI-параметры

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `-w, --width` | Ширина изображения | 1920 |
| `-h, --height` | Высота изображения | 1080 |
| `--seed` | Seed для генератора случайных чисел | 5 |
| `-i, --iteration-count` | Количество итераций | 2500 |
| `-o, --output-path` | Путь к выходному файлу | result.png |
| `-t, --threads` | Количество потоков | 1 |
| `-ap, --affine-params` | Аффинные преобразования | 0.1,0.1,0.1,0.1,0.1,0.1 |
| `-f, --functions` | Вариации и их веса | swirl:1.0 |
| `--config` | Путь к JSON-конфигурации | — |

### Примеры

Генерация с несколькими вариациями:

```bash
java -jar fractal-flame.jar \
  -w 1920 -h 1080 \
  -i 1000000 \
  -f "swirl:0.5,spherical:0.3,sinusoidal:0.2" \
  -ap "0.5,0.3,0.1,0.2,0.6,0.4/0.3,0.7,0.2,0.5,0.1,0.3" \
  -o flame.png
```

Многопоточная генерация:

```bash
java -jar fractal-flame.jar -t 8 -i 5000000 -o fast_flame.png
```

### JSON-конфигурация

```json
{
  "size": {
    "width": 1920,
    "height": 1080
  },
  "seed": 42,
  "iteration_count": 1000000,
  "output_path": "output.png",
  "threads": 4,
  "affine_params": [
    {"a": 0.5, "b": 0.3, "c": 0.1, "d": 0.2, "e": 0.6, "f": 0.4},
    {"a": 0.3, "b": 0.7, "c": 0.2, "d": 0.5, "e": 0.1, "f": 0.3}
  ],
  "functions": [
    {"name": "swirl", "weight": 0.5},
    {"name": "spherical", "weight": 0.3},
    {"name": "sinusoidal", "weight": 0.2}
  ]
}
```

Запуск с конфигурацией:

```bash
java -jar fractal-flame.jar --config config.json
```

## Архитектура

```
academy/
├── Application.java              # Точка входа, CLI-обработка
├── application/
│   ├── algorithm/
│   │   └── ChaosGame.java        # Реализация алгоритма Chaos Game
│   └── render/
│       └── ImageRenderer.java    # Рендеринг и сохранение изображений
├── cli/
│   ├── converter/                # Конвертеры CLI-параметров
│   └── utils/
│       └── CliUtils.java         # Утилиты валидации
└── domain/                       # Доменные модели
    ├── AffineParams.java
    ├── AppConfiguration.java
    ├── Point.java
    ├── Size.java
    ├── Variation.java
    └── VariationParams.java
```

## Алгоритм

1. Инициализация случайной точки в диапазоне [-1, 1]
2. На каждой итерации:
   - Случайный выбор аффинного преобразования
   - Применение аффинной трансформации к точке
   - Применение взвешенной комбинации вариаций
   - Обновление цвета точки (смешивание с цветом трансформации)
   - После warmup-фазы (20 итераций) — запись точки в гистограмму
3. Логарифмическое тональное отображение гистограммы в RGB
4. Сохранение результата в PNG

## Вариации

| Название | Формула |
|----------|---------|
| LINEAR | (x, y) |
| SPHERICAL | (x/r², y/r²) |
| SWIRL | (x·sin(r²) − y·cos(r²), x·cos(r²) + y·sin(r²)) |
| HORSESHOE | ((x−y)(x+y)/r, 2xy/r) |
| EXPONENTIAL | (e^(x−1)·cos(πy), e^(x−1)·sin(πy)) |
| SINUSOIDAL | (sin(x), sin(y)) |

## Многопоточность

При `threads > 1` алгоритм:
- Разделяет итерации между потоками
- Каждый поток работает с локальным `ImageRenderer`
- Использует независимые `Random` с seed + threadIndex
- Финальное слияние гистограмм в основной рендерер

## Зависимости

- [Picocli](https://picocli.info/) — парсинг CLI
- [Lombok](https://projectlombok.org/) — сокращение boilerplate
- [Jackson](https://github.com/FasterXML/jackson) — JSON-сериализация
- [SLF4J](https://www.slf4j.org/) + Log4j2 — логирование
