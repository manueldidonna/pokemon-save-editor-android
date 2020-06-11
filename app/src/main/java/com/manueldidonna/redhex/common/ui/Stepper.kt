package com.manueldidonna.redhex.common.ui

import androidx.animation.*
import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.animation.DpToVectorConverter
import androidx.ui.animation.animatedValue
import androidx.ui.core.Alignment
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.gestures.DragDirection
import androidx.ui.foundation.gestures.draggable
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.*
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.contentColorFor
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ChevronLeft
import androidx.ui.material.icons.twotone.ChevronRight
import androidx.ui.material.ripple.ripple
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

@Composable
fun Stepper(
    modifier: Modifier = Modifier,
    value: Int,
    color: Color = MaterialTheme.colors.primary,
    enableIncrease: Boolean = true,
    enableDecrease: Boolean = true,
    onValueChanged: (Int) -> Unit
) {
    val density = DensityAmbient.current
    Box(modifier) {
        val stepperOffset = stepperAnimatedOffset()
        val animationBuilder = stepperAnimationBuilder()
        val drag = Modifier.draggable(
            dragDirection = DragDirection.Horizontal,
            startDragImmediately = stepperOffset.isRunning,
            onDragDeltaConsumptionRequested = { delta ->
                val newOffset = with(density) {
                    (stepperOffset.value + delta.toDp()).coerceIn(Dp.Hairline, 96.dp)
                }
                stepperOffset.snapTo(newOffset)
                delta
            },
            onDragStopped = {
                val currentOffset = stepperOffset.value
                when {
                    currentOffset <= 20.dp && enableDecrease -> onValueChanged(value - 1)
                    currentOffset >= 76.dp && enableIncrease -> onValueChanged(value + 1)
                }
                stepperOffset.animateTo(48.dp, anim = animationBuilder)
            }
        )
        Stack(Modifier.width(136.dp)) {
            val centerStart = Modifier.gravity(Alignment.CenterStart)
            StepperIcon(
                modifier = centerStart,
                asset = Icons.TwoTone.ChevronLeft,
                enabled = enableDecrease
            )
            StepperValue(
                modifier = drag.plus(centerStart).padding(start = stepperOffset.value),
                value = value,
                color = color
            )
            StepperIcon(
                modifier = Modifier.gravity(Alignment.CenterEnd),
                asset = Icons.TwoTone.ChevronRight,
                enabled = enableIncrease
            )
        }
    }
}

@Composable
private fun stepperAnimatedOffset(): AnimatedValue<Dp, AnimationVector1D> {
    return animatedValue(
        initVal = 48.dp,
        converter = DpToVectorConverter,
        visibilityThreshold = AnimationVector1D(0.1f)
    )
}

@Composable
private fun stepperAnimationBuilder(): AnimationBuilder<Dp> {
    return remember {
        PhysicsBuilder(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }
}

@Composable
private fun StepperValue(
    modifier: Modifier,
    value: Int,
    color: Color
) {
    Surface(
        modifier = modifier,
        elevation = 1.dp,
        color = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            modifier = Modifier
                .size(width = 40.dp, height = 32.dp)
                .wrapContentSize(Alignment.Center)
                .ripple(bounded = false),
            text = value.toString(),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
            color = contentColorFor(color = color)
        )
    }
}

@Composable
private fun StepperIcon(modifier: Modifier, asset: VectorAsset, enabled: Boolean) {
    val emphasis = EmphasisAmbient.current.run { if (enabled) medium else disabled }
    Box(gravity = ContentGravity.Center, modifier = modifier.size(48.dp)) {
        Icon(asset = asset, tint = emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
    }
}

@Preview
@Composable
private fun PreviewCounter() {
    PreviewScreen(colors = LightColors) {
        Box(padding = 24.dp) {
            Stepper(value = 35, onValueChanged = {})
        }
    }
}
