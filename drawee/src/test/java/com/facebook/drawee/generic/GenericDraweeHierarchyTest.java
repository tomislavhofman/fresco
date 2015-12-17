/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.drawee.generic;

import java.util.Arrays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.facebook.drawee.drawable.AndroidGraphicsTestUtils;
import com.facebook.drawee.drawable.DrawableTestUtils;
import com.facebook.drawee.drawable.FadeDrawable;
import com.facebook.drawee.drawable.ForwardingDrawable;
import com.facebook.drawee.drawable.MatrixDrawable;
import com.facebook.drawee.drawable.RoundedBitmapDrawable;
import com.facebook.drawee.drawable.RoundedCornersDrawable;
import com.facebook.drawee.drawable.ScaleTypeDrawable;

import org.robolectric.RobolectricTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class GenericDraweeHierarchyTest {

  private GenericDraweeHierarchyBuilder mBuilder;

  private Drawable mBackground1;
  private Drawable mBackground2;
  private Drawable mOverlay1;
  private Drawable mOverlay2;
  private BitmapDrawable mPlaceholderImage;
  private BitmapDrawable mFailureImage;
  private BitmapDrawable mRetryImage;
  private BitmapDrawable mProgressBarImage;
  private BitmapDrawable mActualImage1;
  private BitmapDrawable mActualImage2;
  private Matrix mActualImageMatrix;
  private PointF mFocusPoint;

  @Before
  public void setUp() {
    mBuilder = new GenericDraweeHierarchyBuilder(null);

    mBackground1 = DrawableTestUtils.mockDrawable();
    mBackground2 = DrawableTestUtils.mockDrawable();
    mOverlay1 = DrawableTestUtils.mockDrawable();
    mOverlay2 = DrawableTestUtils.mockDrawable();
    mPlaceholderImage = DrawableTestUtils.mockBitmapDrawable();
    mFailureImage = DrawableTestUtils.mockBitmapDrawable();
    mRetryImage = DrawableTestUtils.mockBitmapDrawable();
    mProgressBarImage = DrawableTestUtils.mockBitmapDrawable();
    mActualImage1 = DrawableTestUtils.mockBitmapDrawable();
    mActualImage2 = DrawableTestUtils.mockBitmapDrawable();
    mActualImageMatrix = mock(Matrix.class);
    mFocusPoint = new PointF(0.1f, 0.4f);
  }

  @Test
  public void testHierarchy_WithScaleType() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, ScaleType.CENTER)
        .setRetryImage(mRetryImage, ScaleType.FIT_CENTER)
        .setFailureImage(mFailureImage, ScaleType.CENTER_INSIDE)
        .setProgressBarImage(mProgressBarImage, ScaleType.CENTER)
        .setActualImageScaleType(ScaleType.FOCUS_CROP)
        .setActualImageFocusPoint(mFocusPoint)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(5, fadeDrawable.getNumberOfLayers());
    assertScaleTypeAndDrawable(mPlaceholderImage, ScaleType.CENTER, fadeDrawable.getDrawable(0));
    assertActualImageScaleType(ScaleType.FOCUS_CROP, mFocusPoint, fadeDrawable.getDrawable(1));
    assertScaleTypeAndDrawable(mProgressBarImage, ScaleType.CENTER, fadeDrawable.getDrawable(2));
    assertScaleTypeAndDrawable(mRetryImage, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(3));
    assertScaleTypeAndDrawable(mFailureImage, ScaleType.CENTER_INSIDE, fadeDrawable.getDrawable(4));
  }

  @Test
  public void testHierarchy_WithMatrix() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setRetryImage(mRetryImage, null)
        .setFailureImage(mFailureImage, null)
        .setProgressBarImage(mProgressBarImage, null)
        .setActualImageMatrix(mActualImageMatrix)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(5, fadeDrawable.getNumberOfLayers());
    assertSame(mPlaceholderImage, fadeDrawable.getDrawable(0));
    assertSame(mProgressBarImage, fadeDrawable.getDrawable(2));
    assertSame(mRetryImage, fadeDrawable.getDrawable(3));
    assertSame(mFailureImage, fadeDrawable.getDrawable(4));
    MatrixDrawable matrixDrawable = (MatrixDrawable) fadeDrawable.getDrawable(1);
    assertNotNull(matrixDrawable);
    assertEquals(mActualImageMatrix, matrixDrawable.getMatrix());
    assertSame(ForwardingDrawable.class, matrixDrawable.getCurrent().getClass());
  }

  @Test
  public void testHierarchy_NoScaleTypeNorMatrix() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setRetryImage(mRetryImage, null)
        .setFailureImage(mFailureImage, null)
        .setProgressBarImage(mProgressBarImage, null)
        .setActualImageScaleType(null)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(5, fadeDrawable.getNumberOfLayers());
    assertSame(mPlaceholderImage, fadeDrawable.getDrawable(0));
    assertSame(ForwardingDrawable.class, fadeDrawable.getDrawable(1).getClass());
    assertSame(mProgressBarImage, fadeDrawable.getDrawable(2));
    assertSame(mRetryImage, fadeDrawable.getDrawable(3));
    assertSame(mFailureImage, fadeDrawable.getDrawable(4));
  }

  @Test
  public void testHierarchy_NoBranches() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(5, fadeDrawable.getNumberOfLayers());
    assertNull(fadeDrawable.getDrawable(0));
    assertActualImageScaleType(ScaleType.CENTER_CROP, null, fadeDrawable.getDrawable(1));
    assertNull(fadeDrawable.getDrawable(2));
    assertNull(fadeDrawable.getDrawable(3));
    assertNull(fadeDrawable.getDrawable(4));
  }

  @Test
  public void testHierarchy_WithPlaceholderImage() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, ScaleType.CENTER)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertScaleTypeAndDrawable(mPlaceholderImage, ScaleType.CENTER, fadeDrawable.getDrawable(0));
  }

  @Test
  public void testHierarchy_WithFailureImage() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setFailureImage(mFailureImage, ScaleType.CENTER)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertScaleTypeAndDrawable(mFailureImage, ScaleType.CENTER, fadeDrawable.getDrawable(4));
  }

  @Test
  public void testHierarchy_WithRetryImage() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setRetryImage(mRetryImage, ScaleType.CENTER)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertScaleTypeAndDrawable(mRetryImage, ScaleType.CENTER, fadeDrawable.getDrawable(3));
  }

  @Test
  public void testHierarchy_WithProgressBarImage() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setProgressBarImage(mProgressBarImage, ScaleType.CENTER)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertScaleTypeAndDrawable(mProgressBarImage, ScaleType.CENTER, fadeDrawable.getDrawable(2));
  }

  @Test
  public void testHierarchy_WithAllBranches() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, ScaleType.CENTER)
        .setRetryImage(mRetryImage, ScaleType.FIT_CENTER)
        .setFailureImage(mFailureImage, ScaleType.FIT_CENTER)
        .setProgressBarImage(mProgressBarImage, ScaleType.CENTER)
        .setActualImageScaleType(ScaleType.CENTER_CROP)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(5, fadeDrawable.getNumberOfLayers());
    assertScaleTypeAndDrawable(mPlaceholderImage, ScaleType.CENTER, fadeDrawable.getDrawable(0));
    assertActualImageScaleType(ScaleType.CENTER_CROP, null, fadeDrawable.getDrawable(1));
    assertScaleTypeAndDrawable(mProgressBarImage, ScaleType.CENTER, fadeDrawable.getDrawable(2));
    assertScaleTypeAndDrawable(mRetryImage, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(3));
    assertScaleTypeAndDrawable(mFailureImage, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(4));
  }

  @Test
  public void testHierarchy_WithBackgrounds() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setBackgrounds(Arrays.asList(mBackground1, mBackground2))
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(7, fadeDrawable.getNumberOfLayers());
    assertSame(mBackground1, fadeDrawable.getDrawable(0));
    assertSame(mBackground2, fadeDrawable.getDrawable(1));
  }

  @Test
  public void testHierarchy_WithSingleBackground() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setBackground(mBackground1)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(6, fadeDrawable.getNumberOfLayers());
    assertSame(mBackground1, fadeDrawable.getDrawable(0));
  }

  @Test
  public void testHierarchy_WithOverlays() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setOverlays(Arrays.asList(mOverlay1, mOverlay2))
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(7, fadeDrawable.getNumberOfLayers());
    assertSame(mOverlay1, fadeDrawable.getDrawable(5));
    assertSame(mOverlay2, fadeDrawable.getDrawable(6));
  }

  @Test
  public void testHierarchy_WithSingleOverlay() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setOverlay(mOverlay1)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(6, fadeDrawable.getNumberOfLayers());
    assertSame(mOverlay1, fadeDrawable.getDrawable(5));
  }

  @Test
  public void testHierarchy_WithSingleBackgroundAndOverlay() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setBackground(mBackground2)
        .setOverlay(mOverlay2)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(7, fadeDrawable.getNumberOfLayers());
    assertSame(mBackground2, fadeDrawable.getDrawable(0));
    assertSame(mOverlay2, fadeDrawable.getDrawable(6));
  }

  @Test
  public void testHierarchy_WithAllBranchesBackgroundsAndOverlays() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, ScaleType.CENTER)
        .setRetryImage(mRetryImage, ScaleType.FIT_CENTER)
        .setFailureImage(mFailureImage, ScaleType.FIT_CENTER)
        .setProgressBarImage(mProgressBarImage, ScaleType.CENTER)
        .setActualImageScaleType(ScaleType.CENTER_CROP)
        .setBackgrounds(Arrays.asList(mBackground1, mBackground2))
        .setOverlays(Arrays.asList(mOverlay1, mOverlay2))
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(9, fadeDrawable.getNumberOfLayers());
    assertSame(mBackground1, fadeDrawable.getDrawable(0));
    assertSame(mBackground2, fadeDrawable.getDrawable(1));
    assertScaleTypeAndDrawable(mPlaceholderImage, ScaleType.CENTER, fadeDrawable.getDrawable(2));
    assertActualImageScaleType(ScaleType.CENTER_CROP, null, fadeDrawable.getDrawable(3));
    assertScaleTypeAndDrawable(mProgressBarImage, ScaleType.CENTER, fadeDrawable.getDrawable(4));
    assertScaleTypeAndDrawable(mRetryImage, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(5));
    assertScaleTypeAndDrawable(mFailureImage, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(6));
    assertSame(mOverlay1, fadeDrawable.getDrawable(7));
    assertSame(mOverlay2, fadeDrawable.getDrawable(8));
  }

  @Test
  public void testHierarchy_WithPressedStateOverlay() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setOverlay(mOverlay2)
        .setPressedStateOverlay(mOverlay1)
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    FadeDrawable fadeDrawable = (FadeDrawable) rootDrawable.getCurrent();
    assertEquals(7, fadeDrawable.getNumberOfLayers());
    assertSame(mOverlay2, fadeDrawable.getDrawable(5));
    StateListDrawable stateListDrawable = (StateListDrawable) fadeDrawable.getDrawable(6);
    assertNotNull(stateListDrawable);
  }

  @Test
  public void testHierarchy_WithRoundedCornersDrawable() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setRoundingParams(RoundingParams.fromCornersRadius(10).setOverlayColor(0xFFFFFFFF))
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    RoundedCornersDrawable roundedDrawable = (RoundedCornersDrawable) rootDrawable.getCurrent();
    FadeDrawable fadeDrawable = (FadeDrawable) roundedDrawable.getCurrent();
    assertNotNull(fadeDrawable);
  }

  @Test
  public void testHierarchy_WithRoundedCornersDrawableAsCircle() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setRoundingParams(RoundingParams.asCircle().setOverlayColor(0xFFFFFFFF))
        .build();
    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    RoundedCornersDrawable roundedDrawable = (RoundedCornersDrawable) rootDrawable.getCurrent();
    FadeDrawable fadeDrawable = (FadeDrawable) roundedDrawable.getCurrent();
    assertNotNull(fadeDrawable);
  }

  @Test
  public void testControlling_WithPlaceholderOnly() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setActualImageScaleType(null)
        .setFadeDuration(250)
        .build();

    // image indexes in DH tree
    final int placeholderImageIndex = 0;
    final int actualImageIndex = 1;

    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();

    assertEquals(mPlaceholderImage, fadeDrawable.getDrawable(placeholderImageIndex));
    assertEquals(
        ForwardingDrawable.class,
        fadeDrawable.getDrawable(actualImageIndex).getClass());

    ForwardingDrawable actualImageSettableDrawable =
        (ForwardingDrawable) fadeDrawable.getDrawable(actualImageIndex);

    // initial state -> final image (non-immediate)
    // initial state
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set final image (non-immediate)
    dh.setImage(mActualImage1, 1f, false);
    assertEquals(mActualImage1, actualImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> final image (immediate)
    // reset hierarchy to initial state
    dh.reset();
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set final image (immediate)
    dh.setImage(mActualImage2, 1f, true);
    assertEquals(mActualImage2, actualImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());

    // initial state -> retry
    // reset hierarchy to initial state
    dh.reset();
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set retry
    dh.setRetry(new RuntimeException());
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> failure
    // reset hierarchy to initial state
    dh.reset();
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set failure
    dh.setFailure(new RuntimeException());
    assertEquals(ColorDrawable.class, actualImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());
  }

  @Test
  public void testControlling_WithAllLayers() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setBackgrounds(Arrays.asList(mBackground1, mBackground2))
        .setOverlays(Arrays.asList(mOverlay1, mOverlay2))
        .setPlaceholderImage(mPlaceholderImage, null)
        .setRetryImage(mRetryImage, null)
        .setFailureImage(mFailureImage, null)
        .setProgressBarImage(mProgressBarImage, null)
        .setActualImageScaleType(null)
        .setFadeDuration(250)
        .build();

    // image indexes in DH tree
    final int backgroundsIndex = 0;
    final int numBackgrounds = 2;
    final int placeholderImageIndex = numBackgrounds + 0;
    final int actualImageIndex = numBackgrounds + 1;
    final int progressBarImageIndex = numBackgrounds + 2;
    final int retryImageIndex = numBackgrounds + 3;
    final int failureImageIndex = numBackgrounds + 4;
    final int numBranches = 5;
    final int overlaysIndex = numBackgrounds + numBranches;
    final int numOverlays = 2;

    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();

    assertEquals(mPlaceholderImage, fadeDrawable.getDrawable(placeholderImageIndex));
    assertEquals(mProgressBarImage, fadeDrawable.getDrawable(progressBarImageIndex));
    assertEquals(mRetryImage, fadeDrawable.getDrawable(retryImageIndex));
    assertEquals(mFailureImage, fadeDrawable.getDrawable(failureImageIndex));
    assertEquals(
        ForwardingDrawable.class,
        fadeDrawable.getDrawable(actualImageIndex).getClass());

    ForwardingDrawable finalImageSettableDrawable =
        (ForwardingDrawable) fadeDrawable.getDrawable(actualImageIndex);

    // initial state -> final image (immediate)
    // initial state, show progress bar
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set final image (immediate)
    dh.setImage(mActualImage2, 1f, true);
    assertEquals(mActualImage2, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());

    // initial state -> final image (non-immediate)
    // reset hierarchy to initial state, show progress bar
    dh.reset();
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set final image (non-immediate)
    dh.setImage(mActualImage2, 1f, false);
    assertEquals(mActualImage2, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> temporary image (immediate) -> final image (non-immediate)
    // reset hierarchy to initial state, show progress bar
    dh.reset();
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set temporary image (immediate)
    dh.setImage(mActualImage1, 0.5f, true);
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set final image (non-immediate)
    dh.setImage(mActualImage2, 1f, false);
    assertEquals(mActualImage2, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> temporary image (non-immediate) -> final image (non-immediate)
    // reset hierarchy to initial state, show progress bar
    dh.reset();
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set temporary image (non-immediate)
    dh.setImage(mActualImage1, 0.5f, false);
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());
    // set final image (non-immediate)
    dh.setImage(mActualImage2, 1f, false);
    assertEquals(mActualImage2, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> temporary image (immediate) -> retry
    // reset hierarchy to initial state, show progress bar
    dh.reset();
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set temporary image (immediate)
    dh.setImage(mActualImage1, 0.5f, true);
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set retry
    dh.setRetry(new RuntimeException());
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());

    // initial state -> temporary image (immediate) -> failure
    // reset hierarchy to initial state, show progress bar
    dh.reset();
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    dh.setProgress(0f, true);
    assertEquals(ColorDrawable.class, finalImageSettableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set temporary image (immediate)
    dh.setImage(mActualImage1, 0.5f, true);
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());
    // set failure
    dh.setFailure(new RuntimeException());
    assertEquals(mActualImage1, finalImageSettableDrawable.getCurrent());
    assertEquals(false, fadeDrawable.isLayerOn(placeholderImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(actualImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(progressBarImageIndex));
    assertEquals(false, fadeDrawable.isLayerOn(retryImageIndex));
    assertEquals(true, fadeDrawable.isLayerOn(failureImageIndex));
    assertLayersOn(fadeDrawable, backgroundsIndex, numBackgrounds);
    assertLayersOn(fadeDrawable, overlaysIndex, numOverlays);
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());
  }

  @Test
  public void testControlling_WithCornerRadii() throws Exception {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setActualImageScaleType(null)
        .setRoundingParams(RoundingParams.fromCornersRadius(10))
        .setFadeDuration(250)
        .build();

    // image indexes in DH tree
    final int imageIndex = 1;

    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();
    ForwardingDrawable settableDrawable = (ForwardingDrawable) fadeDrawable.getDrawable(imageIndex);

    // set temporary image
    dh.setImage(mActualImage1, 0.5f, true);
    assertNotSame(mActualImage1, settableDrawable.getCurrent());
    assertEquals(RoundedBitmapDrawable.class, settableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(imageIndex));
    assertEquals(FadeDrawable.TRANSITION_NONE, fadeDrawable.getTransitionState());

    // set final image
    dh.setImage(mActualImage2, 1f, false);
    assertNotSame(mActualImage2, settableDrawable.getCurrent());
    assertEquals(RoundedBitmapDrawable.class, settableDrawable.getCurrent().getClass());
    assertEquals(true, fadeDrawable.isLayerOn(imageIndex));
    assertEquals(FadeDrawable.TRANSITION_STARTING, fadeDrawable.getTransitionState());
    assertEquals(250, fadeDrawable.getTransitionDuration());
  }

  @Test
  public void testControlling_WithControllerOverlay() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, null)
        .setActualImageScaleType(null)
        .setFadeDuration(250)
        .build();

    RootDrawable rootDrawable = (RootDrawable) dh.getTopLevelDrawable();
    // set controller overlay
    Drawable controllerOverlay = DrawableTestUtils.mockDrawable();
    dh.setControllerOverlay(controllerOverlay);
    assertSame(controllerOverlay, rootDrawable.mControllerOverlay);

    // clear controller overlay
    dh.setControllerOverlay(null);
    assertNull(rootDrawable.mControllerOverlay);
  }

  private void assertLayersOn(FadeDrawable fadeDrawable, int firstLayerIndex, int numberOfLayers) {
    for (int i = 0; i < numberOfLayers; i++) {
      assertEquals(true, fadeDrawable.isLayerOn(firstLayerIndex + i));
    }
  }

  @Test
  public void testDrawVisibleDrawableOnly() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .build();
    Canvas mockCanvas = mock(Canvas.class);
    dh.getTopLevelDrawable().setVisible(false, true);
    dh.getTopLevelDrawable().draw(mockCanvas);
    verify(mPlaceholderImage, never()).draw(mockCanvas);
    dh.getTopLevelDrawable().setVisible(true, true);
    dh.getTopLevelDrawable().draw(mockCanvas);
    verify(mPlaceholderImage).draw(mockCanvas);
  }

  @Test
  public void testSetPlaceholderImage() throws Exception {
    final GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage, ScaleType.FIT_XY)
        .build();
    testSetDrawable(dh, 0, new SetDrawableCallback() {
      @Override
      public void setDrawable(Drawable drawable) {
        dh.setPlaceholderImage(drawable);
      }
      @Override
      public void setDrawable(Drawable drawable, ScaleType scaleType) {
        dh.setPlaceholderImage(drawable, scaleType);
      }
    });
  }

  @Test
  public void testSetFailureImage() throws Exception {
    final GenericDraweeHierarchy dh = mBuilder
        .setFailureImage(mFailureImage, null)
        .build();
    testSetDrawable(dh, 4, new SetDrawableCallback() {
      @Override
      public void setDrawable(Drawable drawable) {
        dh.setFailureImage(drawable);
      }
      @Override
      public void setDrawable(Drawable drawable, ScaleType scaleType) {
        dh.setFailureImage(drawable, scaleType);
      }
    });
  }

  @Test
  public void testSetRetryImage() throws Exception {
    final GenericDraweeHierarchy dh = mBuilder
        .setRetryImage(mRetryImage, null)
        .build();
    testSetDrawable(dh, 3, new SetDrawableCallback() {
      @Override
      public void setDrawable(Drawable drawable) {
        dh.setRetryImage(drawable);
      }
      @Override
      public void setDrawable(Drawable drawable, ScaleType scaleType) {
        dh.setRetryImage(drawable, scaleType);
      }
    });
  }

  @Test
  public void testSetProgressBarImage() throws Exception {
    final GenericDraweeHierarchy dh = mBuilder
        .setProgressBarImage(mProgressBarImage, null)
        .build();
    testSetDrawable(dh, 2, new SetDrawableCallback() {
      @Override
      public void setDrawable(Drawable drawable) {
        dh.setProgressBarImage(drawable);
      }
      @Override
      public void setDrawable(Drawable drawable, ScaleType scaleType) {
        dh.setProgressBarImage(drawable, scaleType);
      }
    });
  }

  private interface SetDrawableCallback {
    void setDrawable(Drawable drawable);
    void setDrawable(Drawable drawable, ScaleType scaleType);
  }

  private void testSetDrawable(GenericDraweeHierarchy dh, int index, SetDrawableCallback callback) {
    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();
    // null
    callback.setDrawable(null);
    assertNull(fadeDrawable.getDrawable(index));
    // null -> null
    callback.setDrawable(null);
    assertNull(fadeDrawable.getDrawable(index));
    // null -> drawable
    Drawable drawable1 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable1);
    assertSame(drawable1, fadeDrawable.getDrawable(index));
    // drawable -> drawable
    Drawable drawable2 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable2);
    assertSame(drawable2, fadeDrawable.getDrawable(index));
    // drawable -> null
    callback.setDrawable(null);
    assertNull(fadeDrawable.getDrawable(index));
    // null -> scaletype + drawable
    Drawable drawable3 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable3, ScaleType.FOCUS_CROP);
    assertScaleTypeAndDrawable(drawable3, ScaleType.FOCUS_CROP, fadeDrawable.getDrawable(index));
    // scaletype + drawable -> scaletype + drawable
    Drawable drawable4 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable4, ScaleType.CENTER);
    assertScaleTypeAndDrawable(drawable4, ScaleType.CENTER, fadeDrawable.getDrawable(index));
    // scaletype + drawable -> null
    callback.setDrawable(null);
    assertNull(fadeDrawable.getDrawable(index));
    // drawable -> scaletype + drawable
    callback.setDrawable(drawable1);
    Drawable drawable5 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable5, ScaleType.FIT_CENTER);
    assertScaleTypeAndDrawable(drawable5, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(index));
    // scaletype + drawable -> drawable (kep the old scaletype)
    Drawable drawable6 = DrawableTestUtils.mockDrawable();
    callback.setDrawable(drawable6);
    assertScaleTypeAndDrawable(drawable6, ScaleType.FIT_CENTER, fadeDrawable.getDrawable(index));
  }

  @Test
  public void testSetActualImageFocusPoint() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setProgressBarImage(mProgressBarImage)
        .setActualImageScaleType(ScaleType.FOCUS_CROP)
        .build();

    // image indexes in DH tree
    final int imageIndex = 1;

    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();
    ScaleTypeDrawable scaleTypeDrawable = (ScaleTypeDrawable) fadeDrawable.getDrawable(imageIndex);
    assertNull(scaleTypeDrawable.getFocusPoint());

    PointF focus1 = new PointF(0.3f, 0.4f);
    dh.setActualImageFocusPoint(focus1);
    AndroidGraphicsTestUtils.assertEquals(focus1, scaleTypeDrawable.getFocusPoint(), 0f);

    PointF focus2 = new PointF(0.6f, 0.7f);
    dh.setActualImageFocusPoint(focus2);
    AndroidGraphicsTestUtils.assertEquals(focus2, scaleTypeDrawable.getFocusPoint(), 0f);
  }

  @Test
  public void testSetActualImageScaleType() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .build();

    // image indexes in DH tree
    final int imageIndex = 1;

    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();
    ScaleTypeDrawable scaleTypeDrawable = (ScaleTypeDrawable) fadeDrawable.getDrawable(imageIndex);

    ScaleType scaleType1 = ScaleType.FOCUS_CROP;
    dh.setActualImageScaleType(scaleType1);
    assertEquals(scaleType1, scaleTypeDrawable.getScaleType());

    ScaleType scaleType2 = ScaleType.CENTER;
    dh.setActualImageScaleType(scaleType2);
    assertEquals(scaleType2, scaleTypeDrawable.getScaleType());
  }

  @Test
  public void testSetRoundingParams_OverlayColor() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setRoundingParams(RoundingParams.asCircle().setOverlayColor(0xC0123456))
        .build();

    assertEquals(RoundedCornersDrawable.class, dh.getTopLevelDrawable().getCurrent().getClass());

    assertTrue(dh.getRoundingParams().getRoundAsCircle());
    assertEquals(
        RoundingParams.RoundingMethod.OVERLAY_COLOR,
        dh.getRoundingParams().getRoundingMethod());
    assertEquals(0xC0123456, dh.getRoundingParams().getOverlayColor());

    dh.setRoundingParams(RoundingParams.fromCornersRadius(9).setOverlayColor(0xFFFFFFFF));

    assertFalse(dh.getRoundingParams().getRoundAsCircle());
    assertEquals(
        RoundingParams.RoundingMethod.OVERLAY_COLOR,
        dh.getRoundingParams().getRoundingMethod());
    float[] expectedRadii = new float[] {9, 9, 9, 9, 9, 9, 9, 9};
    assertArrayEquals(expectedRadii, dh.getRoundingParams().getCornersRadii(), 0);
    assertEquals(0xFFFFFFFF, dh.getRoundingParams().getOverlayColor());
  }

  @Test
  public void testSetRoundingParams_Border() {
    int borderColor = Color.CYAN;
    float borderWidth = 0.4f;

    RoundingParams roundingParams = RoundingParams
        .asCircle()
        .setOverlayColor(Color.GRAY)
        .setBorder(borderColor, borderWidth);

    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setRoundingParams(roundingParams)
        .build();

    assertEquals(RoundedCornersDrawable.class, dh.getTopLevelDrawable().getCurrent().getClass());

    assertTrue(dh.getRoundingParams().getRoundAsCircle());
    assertEquals(borderColor, dh.getRoundingParams().getBorderColor());
    assertEquals(borderWidth, dh.getRoundingParams().getBorderWidth(), 0);
    assertEquals(Color.GRAY, dh.getRoundingParams().getOverlayColor());

    int borderColor2 = Color.RED;
    float borderWidth2 = 0.3f;
    roundingParams = RoundingParams
        .fromCornersRadius(9)
        .setOverlayColor(Color.RED)
        .setBorder(borderColor2, borderWidth2);

    dh.setRoundingParams(roundingParams);

    assertFalse(dh.getRoundingParams().getRoundAsCircle());

    float[] expectedRadii = new float[] {9, 9, 9, 9, 9, 9, 9, 9};
    assertArrayEquals(expectedRadii, dh.getRoundingParams().getCornersRadii(), 0);
    assertEquals(borderColor2, dh.getRoundingParams().getBorderColor());
    assertEquals(borderWidth2, dh.getRoundingParams().getBorderWidth(), 0);
    assertEquals(Color.RED, dh.getRoundingParams().getOverlayColor());
  }

  @Test
  public void testSetRoundingParams_BitmapOnly() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setRoundingParams(RoundingParams.asCircle())
        .build();

    assertTrue(dh.getRoundingParams().getRoundAsCircle());
    assertEquals(
        RoundingParams.RoundingMethod.BITMAP_ONLY,
        dh.getRoundingParams().getRoundingMethod());

    dh.setRoundingParams(RoundingParams.fromCornersRadius(9));

    assertFalse(dh.getRoundingParams().getRoundAsCircle());
    assertEquals(
        RoundingParams.RoundingMethod.BITMAP_ONLY,
        dh.getRoundingParams().getRoundingMethod());
    float[] expectedRadii = new float[] {9, 9, 9, 9, 9, 9, 9, 9};
    assertArrayEquals(expectedRadii, dh.getRoundingParams().getCornersRadii(), 0);
  }

  @Test
  public void testSetRoundingParamsOverlay_PreviouslyBitmap() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setRoundingParams(RoundingParams.asCircle())
        .build();

    assertTrue(dh.getTopLevelDrawable().getCurrent() instanceof FadeDrawable);
    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent();
    ScaleTypeDrawable placeholderBranch = (ScaleTypeDrawable) fadeDrawable.getDrawable(0);
    assertTrue(placeholderBranch.getCurrent() instanceof RoundedBitmapDrawable);

    dh.setRoundingParams(
        RoundingParams.asCircle().setOverlayColor(Color.BLUE));
    assertTrue(dh.getTopLevelDrawable().getCurrent() instanceof RoundedCornersDrawable);
  }

  @Test
  public void testSetRoundingParamsBitmap_PreviouslyOverlay() {
    GenericDraweeHierarchy dh = mBuilder
        .setPlaceholderImage(mPlaceholderImage)
        .setRoundingParams(RoundingParams.asCircle().setOverlayColor(Color.BLACK))
        .build();

    assertTrue(dh.getTopLevelDrawable().getCurrent() instanceof RoundedCornersDrawable);
    FadeDrawable fadeDrawable = (FadeDrawable) dh.getTopLevelDrawable().getCurrent().getCurrent();
    ScaleTypeDrawable placeholderBranch = (ScaleTypeDrawable) fadeDrawable.getDrawable(0);
    assertFalse(placeholderBranch.getCurrent() instanceof RoundedBitmapDrawable);

    dh.setRoundingParams(RoundingParams.asCircle());
    assertTrue(dh.getTopLevelDrawable().getCurrent() instanceof FadeDrawable);
    placeholderBranch = (ScaleTypeDrawable) fadeDrawable.getDrawable(0);
    assertTrue(placeholderBranch.getCurrent() instanceof RoundedBitmapDrawable);
  }

  private void assertScaleTypeAndDrawable(
      Drawable expectedChild,
      ScaleType expectedScaleType,
      Drawable actualBranch) {
    assertNotNull(actualBranch);
    ScaleTypeDrawable scaleTypeDrawable = (ScaleTypeDrawable) actualBranch;
    assertSame(expectedChild, scaleTypeDrawable.getCurrent());
    assertSame(expectedScaleType, scaleTypeDrawable.getScaleType());
  }

  private void assertActualImageScaleType(
      ScaleType expectedScaleType,
      PointF expectedFocusPoint,
      Drawable actualBranch) {
    assertNotNull(actualBranch);
    ScaleTypeDrawable scaleTypeDrawable = (ScaleTypeDrawable) actualBranch;
    assertSame(expectedScaleType, scaleTypeDrawable.getScaleType());
    assertSame(ForwardingDrawable.class, scaleTypeDrawable.getCurrent().getClass());
    AndroidGraphicsTestUtils.assertEquals(expectedFocusPoint, scaleTypeDrawable.getFocusPoint(), 0);
  }
}