package com.bluelinelabs.conductor.demo.controllers;

import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluelinelabs.conductor.demo.R;
import com.bluelinelabs.conductor.demo.controllers.base.BaseController;
import com.bluelinelabs.conductor.demo.util.BundleBuilder;

import butterknife.BindView;

public class SharedElementDetailController extends BaseController {

    private static final String KEY_TITLE = "SharedElementDetailController.title";
    private static final String KEY_DOT_COLOR = "SharedElementDetailController.dotColor";
    private static final String KEY_FROM_POSITION = "SharedElementDetailController.position";

    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.img_dot) ImageView imgDot;

    public SharedElementDetailController(String title, int dotColor, int fromPosition) {
        this(new BundleBuilder(new Bundle())
                .putString(KEY_TITLE, title)
                .putInt(KEY_DOT_COLOR, dotColor)
                .putInt(KEY_FROM_POSITION, fromPosition)
                .build());
    }

    public SharedElementDetailController(Bundle args) {
        super(args);
    }

    @NonNull
    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.controller_shared_element_detail, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        tvTitle.setText(getArgs().getString(KEY_TITLE));
        imgDot.getDrawable().setColorFilter(ContextCompat.getColor(getActivity(), getArgs().getInt(KEY_DOT_COLOR)), Mode.SRC_ATOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int fromPosition = getArgs().getInt(KEY_FROM_POSITION);
            tvTitle.setTransitionName(getResources().getString(R.string.transition_tag_title_indexed, fromPosition));
            imgDot.setTransitionName(getResources().getString(R.string.transition_tag_dot_indexed, fromPosition));
        }
    }
}
