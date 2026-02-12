package com.digia.digiaui.framework.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalAsset(
        @SerialName("_id") val sId: String,
        val projectId: String,
        val branchId: String,
        val assetType: String,
        val assetData: LocalAssetData,
        val createdAt: String,
        val createdBy: String?,
        val updatedAt: String,
)

@Serializable
data class LocalAssetData(
        val type: String,
        val image: AssetInfo?,
        val fileUrl: AssetInfo?,
        val localPath: String,
)

@Serializable
data class AssetInfo(
        val baseUrl: String,
        val path: String,
        val fileName: String?,
)
