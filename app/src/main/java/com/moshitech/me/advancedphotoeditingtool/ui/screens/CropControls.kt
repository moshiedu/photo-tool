package com.moshitech.me.advancedphotoeditingtool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CropControls(
    scale: Float,
    straightenAngle: Float,
    selectedAspectRatio: AspectRatio,
    showGoldenRatioGuide: Boolean,
    showDiagonalGuide: Boolean,
    showZoomSlider: Boolean,
    onScaleChange: (Float) -> Unit,
    onStraightenAngleChange: (Float) -> Unit,
    onAspectRatioChange: (AspectRatio) -> Unit,
    onShowGoldenRatioGuideChange: (Boolean) -> Unit,
    onShowDiagonalGuideChange: (Boolean) -> Unit,
    onShowZoomSliderChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Crop Options",
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Straighten Slider
        Text(text = "Straighten", color = Color.White)
        Slider(
            value = straightenAngle,
            onValueChange = onStraightenAngleChange,
            valueRange = -45f..45f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Aspect Ratio controls
        Text(text = "Aspect Ratio", color = Color.White)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            AspectRatio.values().forEach { ratio ->
                Button(onClick = { onAspectRatioChange(ratio) }) {
                    if (ratio.name == "SQUARE") {
                        Text("1:1")
                    } else if (ratio.name == "THREE_FOUR") {
                        Text("3:4")
                    } else if (ratio.name == "FOUR_THREE") {
                        Text("4:3")
                    } else if (ratio.name == "SIXTEEN_NINE") {
                        Text("16:9")
                    } else if (ratio.name == "NINE_SIXTEEN") {
                        Text("9:16")
                    } else {
                        Text(ratio.name)
                    }
                }
            }
        }

        // Zoom controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Zoom", color = Color.White)
            IconButton(onClick = { onShowZoomSliderChange(!showZoomSlider) }) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = Color.White)
            }
        }

        if (showZoomSlider) {
            Column {
                Slider(
                    value = scale,
                    onValueChange = onScaleChange,
                    valueRange = 1f..5f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(scale * 100).roundToInt()}%",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        // Guide toggles
        Text(text = "Guides", color = Color.White)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { onShowGoldenRatioGuideChange(!showGoldenRatioGuide) }) {
                Text("Golden Ratio")
            }
            Button(onClick = { onShowDiagonalGuideChange(!showDiagonalGuide) }) {
                Text("Diagonal Guides")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Button(onClick = onReset) {
                Text("Reset")
            }
            Button(onClick = onApply) {
                Text("Apply")
            }
        }
    }
}