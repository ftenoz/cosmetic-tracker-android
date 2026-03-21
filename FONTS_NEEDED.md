# Font Files Needed

Download and add these font files to `app/src/main/res/font/`:

## Plus Jakarta Sans
Download from: https://fonts.google.com/specimen/Plus+Jakarta+Sans

Required files:
- `plus_jakarta_sans_regular.ttf`
- `plus_jakarta_sans_medium.ttf`
- `plus_jakarta_sans_semibold.ttf`
- `plus_jakarta_sans_bold.ttf`

## Inter
Download from: https://fonts.google.com/specimen/Inter

Required files:
- `inter_regular.ttf`
- `inter_medium.ttf`
- `inter_semibold.ttf`
- `inter_bold.ttf`

## Alternative: Use Default Fonts

If you don't want to add custom fonts, you can update `Type.kt`:

```kotlin
val PlusJakartaSans = FontFamily.Default
val Inter = FontFamily.Default
```

This will use system fonts instead.
