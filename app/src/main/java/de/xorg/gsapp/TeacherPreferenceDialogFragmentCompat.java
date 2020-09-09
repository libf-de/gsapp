package de.xorg.gsapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.preference.PreferenceDialogFragmentCompat;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;


    /*public AutoCompletePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEditText = new AutoCompleteTextView(context, attrs);
        mEditText.setThreshold(0);
        //The adapter of your choice
        TeacherAdapter ta = new TeacherAdapter(getContext());
        mEditText.setAdapter(ta);
    }

    @Override
    protected void onBindDialogView(View view) {

        mEditText = new AutoCompleteTextView(context, attrs);
        mEditText.setThreshold(0);
        //The adapter of your choice
        TeacherAdapter ta = new TeacherAdapter(getContext());
        mEditText.setAdapter(ta);

        mEditText.setText(mText);

        ViewParent oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }*/

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

    public class TeacherPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
        private static final String SAVE_STATE_TEXT = "TeacherPreferenceDialogFragment.text";
        private AppCompatAutoCompleteTextView mEditText;
        //private AutoComEditText mEditText;
        private CharSequence mText;

        public static TeacherPreferenceDialogFragmentCompat newInstance(String key) {
            final TeacherPreferenceDialogFragmentCompat fragment = new TeacherPreferenceDialogFragmentCompat();
            final Bundle b = new Bundle(1);
            b.putString(ARG_KEY, key);
            fragment.setArguments(b);
            return fragment;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                mText = getTeacherPreference().getText();
            } else {
                mText = savedInstanceState.getCharSequence(SAVE_STATE_TEXT);
            }
        }
        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putCharSequence(SAVE_STATE_TEXT, mText);
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);

            //mEditText = view.findViewById(android.R.id.edit);

            AppCompatAutoCompleteTextView acv = new AppCompatAutoCompleteTextView(Objects.requireNonNull(getContext()));
            acv.setThreshold(0);
            int marg = Util.convertToPixels(getContext(), -4);
            LinearLayout.LayoutParams acvp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            acvp.setMargins(marg, 0, marg, 0);
            acv.setLayoutParams(acvp);

            new ViewGroupUtils().replaceView(view.findViewById(android.R.id.edit), acv);

            mEditText = acv;

            mEditText.requestFocus();
            mEditText.setText(mText);
            mEditText.setThreshold(0);
            mEditText.setAdapter(new TeacherAdapter(getContext()));
            // Place cursor at the end
            mEditText.setSelection(mEditText.getText().length());



            if (getTeacherPreference().getOnBindTeacherListener() != null) {
                getTeacherPreference().getOnBindTeacherListener().onBindEditText(mEditText);
            }
        }
        private TeacherPreference getTeacherPreference() {
            return (TeacherPreference) getPreference();
        }

        @Override
        protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            builder.setPositiveButton("Okk", (dialog, which) -> {
                if(mEditText != null)
                    if(mEditText.getText().length() > 4 || mEditText.getText().length() < 3)
                        new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                                .setTitle("Falsche Eingabe!")
                                .setMessage("Für diese Funktion müssen Sie ihr Namenskürzel (3 oder 4 Buchstaben) eingeben. Das Textfeld zeigt während des Tippens Vorschläge an, die durch Tippen auf den jeweiligen Vorschlag übernommen werden können.\n\nDer Lehrerfilter wurde aufgrund der falschen Eingabe deaktiviert. Geben Sie ihr Kürzel erneut im richtigen Format ein, um den Filter zu aktivieren.")
                                .setNeutralButton("OK", (dialog1, which1) -> dialog1.dismiss()).create().show();
            });
        }



        /** @hide */
        @RestrictTo(LIBRARY_GROUP)
        @Override
        protected boolean needInputMethod() {
            // We want the input method to show, if possible, when dialog is displayed
            return false;
        }
        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                String value = mEditText.getText().toString();
                if (getTeacherPreference().callChangeListener(value)) {
                    getTeacherPreference().setText(value);
                }
            }
        }
        private class TeacherAdapter extends BaseAdapter implements Filterable {

            private Context mContext;
            private List<Map.Entry<String, String>> resultList = new ArrayList<>();

            TeacherAdapter(Context context) {
                mContext = context;
            }

            @Override
            public int getCount() {
                return resultList.size();
            }

        /*@Override
        public Pair<String, String> getItem(int index) {

            return new Pair<>(new ArrayList<>(resultList.keySet()).get(index), new ArrayList<>(resultList.values()).get(index));
        }*/

            public Map.Entry<String, String> getItem(int index) {
                return resultList.get(index);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.simple_dropdown_item_2line, parent, false);
                }
                ((TextView) convertView.findViewById(R.id.text1)).setText(getItem(position).getKey());
                ((TextView) convertView.findViewById(R.id.text2)).setText(getItem(position).getValue());
                return convertView;
            }

            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults filterResults = new FilterResults();
                        if (constraint != null) {
                            List<Map.Entry<String, String>> suggestions = new ArrayList<>();
                            for (Map.Entry<String, String> entry : Util.TEACHERS.entrySet()) {
                                if(entry.getKey().toLowerCase().contains(constraint.toString().toLowerCase()) || entry.getValue().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                //if(entry.getKey().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                                    suggestions.add(entry);
                                }
                            }
                            filterResults.values = suggestions;
                            filterResults.count = suggestions.size();
                        }
                        return filterResults;
                    }

                    @Override
                    public String convertResultToString(Object resultValue) {
                        return ((Map.Entry<String,String>)(resultValue)).getKey();
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        if (results != null && results.count > 0) {
                            resultList = (List<Map.Entry<String, String>>) results.values;
                            notifyDataSetChanged();
                        } else {
                            notifyDataSetInvalidated();
                        }
                    }};
            }
        }


        private class ViewGroupUtils {

            ViewGroup getParent(View view) {
                return (ViewGroup)view.getParent();
            }

            void removeView(View view) {
                ViewGroup parent = getParent(view);
                if(parent != null) {
                    parent.removeView(view);
                }
            }

            void replaceView(View currentView, View newView) {
                ViewGroup parent = getParent(currentView);
                if(parent == null) {
                    return;
                }
                final int index = parent.indexOfChild(currentView);
                removeView(currentView);
                removeView(newView);
                parent.addView(newView, index);
            }
        }
    }
