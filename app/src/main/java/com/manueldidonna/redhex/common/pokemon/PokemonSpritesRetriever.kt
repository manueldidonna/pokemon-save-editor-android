package com.manueldidonna.redhex.common.pokemon

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.Composable
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.core.Modifier
import androidx.ui.layout.preferredSize
import androidx.ui.unit.Size
import androidx.ui.unit.dp
import com.manueldidonna.redhex.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun Modifier.pokemonSpriteSize(): Modifier {
    return preferredSize(PokemonSpriteSize.width, PokemonSpriteSize.height)
}

val PokemonSpriteSize: Size = Size(48.dp, 32.dp)

class PokemonSpritesRetriever private constructor(private val spritesFolderPath: String) {

    fun getSpritesPathFromId(speciesId: Int): String {
        return "$spritesFolderPath${File.separator}$speciesId.png"
    }

    companion object {
        private const val SpritesCount = 151
        private const val SpriteWidth = 40
        private const val SpriteHeight = 30

        fun from(context: Context): PokemonSpritesRetriever {
            val spritesFolder = File(context.filesDir, "sprites")
            if (!spritesFolder.exists()) {
                spritesFolder.mkdir()
                val sprites = context.getDrawable(R.drawable.gen1_sprites)!!.toBitmap()
                splitSprites(
                    spritesFolder,
                    sprites
                )
            }
            return PokemonSpritesRetriever(spritesFolder.path)
        }

        private fun splitSprites(spritesFolder: File, sprites: Bitmap) {
            GlobalScope.launch(Dispatchers.IO) {
                var x = 0
                var y = 0
                for (i in 1..SpritesCount) {
                    File(spritesFolder, "$i.png").outputStream().use { output ->
                        Bitmap
                            .createBitmap(sprites, x, y, SpriteWidth, SpriteHeight)
                            .compress(Bitmap.CompressFormat.PNG, 100, output)
                    }
                    x += SpriteWidth
                    if (x >= sprites.width) {
                        x = 0
                        y += SpriteHeight
                    }
                }
            }
        }
    }
}