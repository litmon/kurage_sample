package com.example.linearlayout_sample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	// 選択中の音符の種類
	NoteType selectNoteType;

	HorizontalScrollView scoreScrollView;
	LinearLayout scoreLinearLayout;
	OnClickListener mOnLineClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Context context = v.getContext();

			LinearLayout selectLine = (LinearLayout) v;
			LinearLayout measure = (LinearLayout) v.getParent();

			// 音符の総数を数える(16分音符が何個分あるか)
			int weightSum = 0;
			for (int i = 0; i < selectLine.getChildCount(); i++) {
				weightSum += getWeight(selectLine.getChildAt(i));
			}

			// 音符が置けるかチェック
			float weight = selectNoteType.getWeight();
			if (weight > 16 - weightSum) {
				Toast.makeText(v.getContext(), "この音符はここには置けません", Toast.LENGTH_SHORT).show();
				return;
			}

			// 音符用のLayoutParamsを作成
			LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
			params.weight = weight;

			// すべてのlineに対して音符を追加
			for (int i = 0; i < measure.getChildCount(); i++) {
				// i番目のlineを取得
				LinearLayout line = (LinearLayout) measure.getChildAt(i);

				// 追加する音符を作成
				ImageView note = new ImageView(context);

				// 音符の大きさを指定
				note.setLayoutParams(params);
				note.setOnClickListener(mOnNoteClickListener);

				// 選択したlineの音符には画像をつける
				if (line == v) {
					note.setImageResource(selectNoteType.getResourceId());
				}

				line.addView(note);
			}
			// weightSumの更新
			weightSum += weight;

			// 音符の総数が小節の最大数を超えたら小節を追加
			// if (weightSum >= selectLine.getWeightSum()) {
			// addMeasure(v);
			// }
		}
	};

	OnClickListener mOnNoteClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ImageView clickNote = (ImageView) v;
			LinearLayout clickLine = (LinearLayout) v.getParent();
			LinearLayout clickMeasure = (LinearLayout) clickLine.getParent();

			/* 置けるかどうかチェックする */
			float restWeight = NoteType.MAX_SIZE;
			for (int i = 0; i < clickLine.indexOfChild(clickNote); i++) {
				restWeight -= getWeight(clickLine.getChildAt(i));
			}

			if (restWeight < selectNoteType.getWeight()) {
				Toast.makeText(v.getContext(), "ここにはこの音符は置けません", Toast.LENGTH_SHORT).show();
				return;
			}

			float selectNoteTypeWeight = selectNoteType.getWeight();
			float clickNoteWeight = getWeight(clickNote);

			if (clickNoteWeight < selectNoteTypeWeight) {
				Toast.makeText(v.getContext(), "ここにはこの音符は置けません", Toast.LENGTH_SHORT).show();
				return;
			} else if (clickNoteWeight >= selectNoteTypeWeight) {
				int rate = (int) (clickNoteWeight / selectNoteTypeWeight);
				getParams(clickNote).weight = selectNoteTypeWeight;

				// line内のpositionを取得
				int pos = clickLine.indexOfChild(clickNote);

				// 音符用のLayoutParamsを作成
				LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
				params.weight = selectNoteTypeWeight;

				// すべてのlineに対して
				for (int i = 0; i < clickMeasure.getChildCount(); i++) {
					LinearLayout line = (LinearLayout) clickMeasure.getChildAt(i);
					{
						ImageView note = (ImageView) line.getChildAt(pos);
						if (note == clickNote) {
							note.setImageResource(selectNoteType.getResourceId());
						} else {
							note.setImageBitmap(null);
						}
					}

					for (int j = 1; j < rate; j++) {
						// 追加する音符を作成
						ImageView note = new ImageView(v.getContext());

						// 音符の大きさを指定
						note.setLayoutParams(params);
						note.setOnClickListener(mOnNoteClickListener);

						// 選択したlineの音符には画像をつける
						if (line == clickLine) {
							note.setImageResource(selectNoteType.getResourceId());
						}

						line.addView(note, pos);
					}
				}
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		scoreScrollView = (HorizontalScrollView) findViewById(R.id.scoreScrollView);
		scoreLinearLayout = (LinearLayout) findViewById(R.id.scoreLinearLayout);
		LinearLayout firstMeasure = (LinearLayout) findViewById(R.id.firstMeasure);

		// 初めの小節にonClickを実装する
		setOnLineClickListener(firstMeasure, mOnLineClickListener);

		// 音符の初期設定(8分音符)
		selectNoteType = NoteType.EIGHTH;
	}

	public void onSelectNoteEighth(View v) {
		selectNoteType = NoteType.SIXTEENTH;
	}

	public void onSelectNoteQuarter(View v) {
		selectNoteType = NoteType.QUARTER;
	}

	public void addMeasure(View v) {
		final LinearLayout measure = createMeasure(v.getContext());
		scoreLinearLayout.addView(measure);

		new Handler().post(new Runnable() {

			@Override
			public void run() {
				scoreScrollView.smoothScrollBy(measure.getWidth(), 0);
			}
		});
	}

	public LinearLayout createMeasure(Context context) {
		// measureのViewを作成
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout measure = (LinearLayout) inflater.inflate(R.layout.layout_measure, null);

		measure.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setOnLineClickListener(measure, mOnLineClickListener);

		return measure;
	}

	private void setOnLineClickListener(LinearLayout measure, OnClickListener listener) {
		for (int i = 0; i < measure.getChildCount(); i++) {
			measure.getChildAt(i).setOnClickListener(listener);
		}
	}

	public float getWeight(View v) {
		return getParams(v).weight;
	}

	public LinearLayout.LayoutParams getParams(View v) {
		return (LayoutParams) v.getLayoutParams();
	}
}
