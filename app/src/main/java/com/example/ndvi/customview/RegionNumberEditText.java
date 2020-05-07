package com.example.ndvi.customview;

/**
 * @author: majin
 * @date: 2019/8/6$
 * @desc:
 */

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

import baseLibrary.toast.ToastUtil;

/**
 * Created by Majin
 * 限制输入数字的范围
 */
public class RegionNumberEditText extends android.support.v7.widget.AppCompatEditText {
    private Context context;
    private double  max;
    private double  min;

    public RegionNumberEditText(Context context) {
        super(context);
        this.context = context;
    }

    public RegionNumberEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public RegionNumberEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    /**
     * 设置输入数字的范围
     *
     * @param maxNum 最大数
     * @param minNum 最小数
     */
    public void setRegion(int maxNum, int minNum) {
        //  setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        this.max = maxNum;
        this.min = minNum;
    }

    public void setTextWatcher() {
        setEditTextInhibitInputSpace();
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start >= 0) {//从一输入就开始判断，
                    if (min != - 2 && max != - 2) {
                        try {

                            double num = Double.parseDouble(s.toString());
                            //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                            if (num > max) {
                                s = String.valueOf(max);//如果大于max，则内容为max
                                setText(s);
                                ToastUtil.showToast(context, "最大值是1");
                            } else if (num < min) {
                                s = String.valueOf(min);//如果小于min,则内容为min
                                setText(s);
                                ToastUtil.showToast(context, "最小值是-1");
                            }
                            setSelection(s.length());
                        } catch (NumberFormatException e) {
                            Log.e("ontextchanged", "==" + e.toString());
                        }
                        //edittext中的数字在max和min之间，则不做处理，正常显示即可。
                        return;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * 禁止EditText输入空格
     */
    public void setEditTextInhibitInputSpace() {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.toString().startsWith(".")) {
                    setText(0 + source.toString());
                }
                if (source.equals(" ")) return "";
                else return null;
            }
        };
        setFilters(new InputFilter[]{filter});
    }

}
