package fr.traore.adama.ubitransportspeedmeter.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.traore.adama.ubitransportspeedmeter.R;
import fr.traore.adama.ubitransportspeedmeter.helper.MathHelper;

public class SummaryActivity extends AppCompatActivity {

    private static final String EXTRA_LIST_SPEED = "speedlist";
    private List<Integer> mListSpeed = null;
    @BindView(R.id.txvAverageSpeed) TextView txvAverageSpeed;

    public static void launch(Context context, ArrayList<Integer> listSpeed){
        Intent intent = new Intent(context, SummaryActivity.class);
        intent.putIntegerArrayListExtra(EXTRA_LIST_SPEED, listSpeed);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ButterKnife.bind(this);

        if(getIntent().hasExtra(EXTRA_LIST_SPEED)){
            mListSpeed = getIntent().getIntegerArrayListExtra(EXTRA_LIST_SPEED);
        }

        txvAverageSpeed.setText(String.valueOf(MathHelper.calculateAverageFromIntegerList(mListSpeed)));
    }

}