package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/*

1. set list of words to generate the random problems
2. When starting a game, randomly select the word from the list
3. produce the text views by the length of word (for loop)
4. check if the the selected letter is in the string, then disable the button
5. if the selected letter matches with letter, player gets point, else update the stage of imgHangman






 */

public class MainActivity extends AppCompatActivity {

    // I use an ArrayList here to make shuffling easier and avoid repeating consecutive words
    private ArrayList<String> questions = new ArrayList<String>(Arrays.asList("apple", "cucumber", "desk", "football", "zebra", "shark", "boston")); // array of question sets
    private String question;
    private TextView[] textViews; // array of text view
    private Button[] buttons;

    private LinearLayout LLMain;
    private LinearLayout.LayoutParams LLP;
    private LinearLayout scrollLetters;
    private int index;
    private ArrayList<String> chosenLetters;
    private int remainingLetters;
    private int hangState;
    private int score;
    private boolean hintPressed;

    private Button btnRestart;
    private Button btnHint;
    private TextView txtHint;
    private ImageView imgHangman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollLetters = (LinearLayout) findViewById(R.id.linearlayout);

        chosenLetters = new ArrayList<>();

        int orientation = this.getResources().getConfiguration().orientation;


        // Restore instances or shuffle questions
        if(savedInstanceState == null){
            Log.i("tag", "saved state is null");
            Collections.shuffle(questions);
            index = 0;
            hangState = 0;
            score = 0;
            hintPressed = false;
        }
        else {
            restoreBundle(savedInstanceState);
        }

        question = questions.get(index);
        remainingLetters = question.length();

        displayButtons();

        LLMain = new LinearLayout(MainActivity.this);
        LLMain.setOrientation(LinearLayout.HORIZONTAL);

        LLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        displayQuestion();

        imgHangman = findViewById(R.id.imgHangman);

        updateMan();

        btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementQuestion();
                chosenLetters.clear();
                clearViews();
                hangState = 0;
                updateMan();
                displayButtons();
                displayQuestion();
            }
        });

        if (orientation == Configuration.ORIENTATION_PORTRAIT){ // portrait mode
            LLP.setMargins(50,600,60,50);
            this.addContentView(LLMain,LLP);
        }

        else{
            LLP.setMargins(50,200,50,100); // landscape mode
            this.addContentView(LLMain,LLP);
            txtHint = findViewById(R.id.txtHint);
            btnHint = findViewById(R.id.btnHint);

            btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    incrementQuestion();
                    btnHint.setEnabled(true);
                    txtHint.setText("");
                    chosenLetters.clear();
                    clearViews();
                    hangState = 0;
                    updateMan();
                    displayButtons();
                    displayQuestion();
                }
            });

            btnHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    txtHint.setText(generateHint(question));
                    hintPressed = true;
                    btnHint.setEnabled(false);
                }
            });
        }

    }

    private void makeBundle(Bundle b) {
        b.putInt("index", index);
        b.putStringArrayList("questions", questions);
        b.putStringArrayList("chosen", chosenLetters);
        b.putInt("remaining", remainingLetters);
        b.putInt("state", hangState);
        b.putInt("score", score);
        b.putBoolean("hint", hintPressed);
    }

    private void restoreBundle(Bundle b) {
        index = b.getInt("index");
        questions = b.getStringArrayList("questions");
        chosenLetters = b.getStringArrayList("chosen");
        remainingLetters = b.getInt("remaining");
        hangState = b.getInt("state");
        score = b.getInt("score");
        hintPressed = b.getBoolean("hint");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        makeBundle(outState);

        super.onSaveInstanceState(outState);
    }
    //restore the data during the rotation
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        restoreBundle(savedInstanceState);
        restoreLetters();
        if(btnHint != null && hintPressed) {
            btnHint.setEnabled(false);
            txtHint.setText(generateHint(question));
        }

    }

    public void checkLetters(String ch) {
        String c = String.valueOf(Character.toLowerCase(ch.charAt(0)));
        chosenLetters.add(c);
        if(question.contains(c)) {
            for(int i = 0; i < question.length(); i++) {
                if(question.charAt(i) == c.charAt(0)) {
                    if(c.equals("a") || c.equals("e") || c.equals("i") || c.equals("o") || c.equals("u")) score += 5;
                    else score += 2;
                    remainingLetters--;
                    textViews[i].setText(c);
                    checkWin();
                }
            }
        }
        else {
            incrementHangstate();
            updateMan();
        }
    }

    private void restoreLetters() {
        for(String s : chosenLetters) {
            if(question.contains(s)) {
                for(int i = 0; i < question.length(); i++) {
                    if(question.charAt(i) == s.charAt(0)) {
                        textViews[i].setText(s);
                    }
                }
            }
        }
    }

    private void checkWin() {
        if(remainingLetters <= 0) {
            Toast.makeText(getApplicationContext(), String.format("You win! Score: %d", score), Toast.LENGTH_LONG).show();
            hangState = 0;
            updateMan();
            score = 0;
            incrementQuestion();
            chosenLetters.clear();
            clearViews();
            if(btnHint != null) btnHint.setEnabled(true);
            if(txtHint != null) txtHint.setText("");
            displayQuestion();
            displayButtons();
        }
    }

    private void clearViews() {
        for(View v : textViews) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
        for(View v : buttons) {
            ((ViewGroup) v.getParent()).removeView(v);
        }
    }

    private void displayQuestion() {
        question = questions.get(index);
        remainingLetters = question.length();

        textViews = new TextView[question.length()];

        for (int i = 0; i < textViews.length; i++) {
            textViews[i] = new TextView(getApplicationContext());
            textViews[i].setText("_");
            textViews[i].setTag(i);
            textViews[i].setTextSize(25);
            textViews[i].setLayoutParams(LLP);
            LLMain.addView(textViews[i]);
        }
    }

    private void displayButtons() {
        buttons = new Button[26];
        int temp = 0;

        for(char c = 'A'; c <= 'Z'; ++c ){
            Button b = new Button(getApplicationContext());
            b.setText(String.valueOf(c));
            b.setOnClickListener(getOnClick(b));
            if(chosenLetters.contains(String.valueOf(Character.toLowerCase(c)))) b.setEnabled(false);
            buttons[temp] = b;
            temp++;
            scrollLetters.addView(b);
        }
    }

    private String generateHint(String q) {
        switch (q) {
            case "apple":
                return "reddish fruit";
            case "cucumber":
                return "green vegetable";
            case "desk":
                return "table";
            case "football":
                return "Soccer";
            case "zebra":
                return "Striped horse";
            case "shark":
                return "Jaws";
            case "boston":
                return "Best city";
        }
        return "No hint";
    }

    private void updateMan() {

        switch(hangState) {
            case 0:
                imgHangman.setImageResource(R.drawable.stage0);
                break;
            case 1:
                imgHangman.setImageResource(R.drawable.stage1);
                break;
            case 2:
                imgHangman.setImageResource(R.drawable.stage2);
                break;
            case 3:
                imgHangman.setImageResource(R.drawable.stage3);
                break;
            case 4:
                imgHangman.setImageResource(R.drawable.stage4);
                break;
            case 5:
                imgHangman.setImageResource(R.drawable.stage5);
                break;
            case 6:
                imgHangman.setImageResource(R.drawable.stage6);
                Toast.makeText(getApplicationContext(), String.format("You lost! Score: %d\n Please press the restart button", score), Toast.LENGTH_LONG).show();
                for(Button b : buttons) {
                    b.setEnabled(false);
                }
                if(btnHint != null) btnHint.setEnabled(false);
                break;
        }

    }

    private void incrementHangstate() {
        if(hangState < 6) hangState++;
    }

    private void incrementQuestion() {
        if (index < questions.size() - 1) {
            index++;
        }
        else {
            Toast.makeText(getApplicationContext(), "You've guessed all of our words!\nShuffling words and restarting...", Toast.LENGTH_LONG).show();
            index = 0;
            Collections.shuffle(questions);
        }
    }

    View.OnClickListener getOnClick(final Button button)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                checkLetters(String.valueOf(Character.toLowerCase(button.getText().charAt(0))));
                button.setEnabled(false);
            }
        };
    }
}