package com.manueldidonna.redhex.ui.importsavedata

import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.gsc.GSCSaveDataFactory
import com.manueldidonna.pk.rby.RBYSaveDataFactory

object SaveDataFactory : SaveData.Factory {

    private val factories = listOf(
        RBYSaveDataFactory,
        GSCSaveDataFactory
    )

    override fun create(data: UByteArray): SaveData? {
        for (factory in factories) {
            val saveData = factory.create(data)
            if (saveData != null)
                return saveData
        }
        return null
    }
}
