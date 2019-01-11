package de.xorg.gsapp;

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.preference.DialogPreference;
import androidx.preference.EditTextPreference;

/**
 * A {@link DialogPreference} that shows a {@link EditText} in the dialog.
 *
 * <p>This preference saves a string value.
 */
public class TeacherPreference extends EditTextPreference {
    private String mText;
    @Nullable
    private OnBindTeacherListener mOnBindTeacherListener;

    public TeacherPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TeacherPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TeacherPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TeacherPreference(Context context) {
        super(context);
    }


    /**
     * Set an {@link OnBindTeacherListener} that will be invoked when the corresponding dialog
     * view for this preference is bound. Set {@code null} to remove the existing
     * OnBindEditTextListener.
     *
     * @param onBindTeacherListener The {@link OnBindTeacherListener} that will be invoked when
     *                               the corresponding dialog view for this preference is bound
     * @see OnBindTeacherListener
     */
    public void setOnBindTeacherListener(@Nullable OnBindTeacherListener onBindTeacherListener) {
        mOnBindTeacherListener = onBindTeacherListener;
    }
    /**
     * Returns the {@link OnBindTeacherListener} used to configure the {@link EditText}
     * displayed in the corresponding dialog view for this preference.
     *
     * @return The {@link OnBindTeacherListener} set for this preference, or {@code null} if
     * there is no OnBindEditTextListener set
     * @see OnBindTeacherListener
     */
    public @Nullable OnBindTeacherListener getOnBindTeacherListener() {
        return mOnBindTeacherListener;
    }
    /**
     * Interface definition for a callback to be invoked when the corresponding dialog view for
     * this preference is bound. This allows you to customize the {@link EditText} displayed
     * in the dialog, such as setting a max length or a specific input type.
     */
    public interface OnBindTeacherListener {
        /**
         * Called when the dialog view for this preference has been bound, allowing you to
         * customize the {@link EditText} displayed in the dialog.
         *
         * @param editText The {@link EditText} displayed in the dialog
         */
        void onBindEditText(@NonNull AppCompatAutoCompleteTextView editText);
    }
}