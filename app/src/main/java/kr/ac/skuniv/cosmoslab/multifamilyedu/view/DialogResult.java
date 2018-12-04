package kr.ac.skuniv.cosmoslab.multifamilyedu.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import kr.ac.skuniv.cosmoslab.multifamilyedu.R;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.AnalysisWaveFormController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.controller.SettingForAnalysisController;
import kr.ac.skuniv.cosmoslab.multifamilyedu.model.WaveFormModel;

/**
 * Created by chunso on 2018-12-04.
 */

public class DialogResult extends DialogFragment {
    private final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MultiFamily";
    String mOriginalPath;
    String mRecordPath;
    String mWord;
    int mFinalScore;

    public interface OnCompleteListener{
        void onReplay(int score);
        void onNext(String complete, int score);
    }

    private OnCompleteListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnCompleteListener) activity;
        }
        catch (ClassCastException e) {
            Log.d("DialogFragmentExample", "Activity doesn't implement the OnCompleteListener interface");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_result, null);
        builder.setView(view);

        Bundle args = getArguments();
        setPath(args.getString("word"));

        final Button replayBtn = view.findViewById(R.id.replayBtn);
        final Button nextBtn = view.findViewById(R.id.nextBtn);
        final ImageView imageView = view.findViewById(R.id.waveformImg);
        final TextView scoreTV = view.findViewById(R.id.scoreTV);
        imageView.setImageBitmap(onDraw());
        scoreTV.setText(mFinalScore+"점");

        System.out.println("점수: "+mFinalScore);

        replayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
                mCallback.onReplay(mFinalScore);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mCallback.onNext("next", mFinalScore);
            }
        });

        return builder.create();
    }
    public Bitmap onDraw() {
        SettingForAnalysisController settingForAnalysis = new SettingForAnalysisController(mOriginalPath, mRecordPath);

        settingForAnalysis.controller();

        WaveFormModel originalModel = settingForAnalysis.getmOriginalData();
        WaveFormModel recodeModel = settingForAnalysis.getmRecodeData();

        AnalysisWaveFormController analysisWaveform = new AnalysisWaveFormController(originalModel, recodeModel);
        mFinalScore = analysisWaveform.getFinalScore();

        originalModel = analysisWaveform.getmOriginalData();
        recodeModel = analysisWaveform.getmRecodeData();

        int bitmapX = originalModel.getWaveData().length > recodeModel.getWaveData().length ? originalModel.getWaveData().length : recodeModel.getWaveData().length;
        int bitmapY = originalModel.getWaveData()[originalModel.getMaximumValueIndex()] > recodeModel.getWaveData()[recodeModel.getMaximumValueIndex()]
                ? originalModel.getWaveData()[originalModel.getMaximumValueIndex()] : recodeModel.getWaveData()[recodeModel.getMaximumValueIndex()];

        Bitmap waveForm = Bitmap.createBitmap(bitmapX, bitmapY, Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(waveForm);
        Canvas recodeCanvas = new Canvas(waveForm);

        Paint originalWaveform = new Paint();
        originalWaveform.setColor(Color.BLUE);
        originalWaveform.setAlpha(40);

        Paint recodeWaveform = new Paint();
        recodeWaveform.setColor(Color.RED);
        recodeWaveform.setAlpha(60);

        for (int i = 0; i < bitmapX; i++) {
            originalCanvas.drawLine(i, bitmapY - originalModel.getWaveData()[i], i, bitmapY, originalWaveform);
            recodeCanvas.drawLine(i, bitmapY - recodeModel.getWaveData()[i], i, bitmapY, recodeWaveform);
        }

        return waveForm;
    }

    public void setPath(String word){
        mWord = word;
        mOriginalPath = FILE_PATH+"/ORIGINAL/" + mWord + ".wav";
        mRecordPath = FILE_PATH+"/RECORD/" + mWord + ".wav";
    }
}