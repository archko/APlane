package cn.archko.microblog.filter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class EditFilter extends Activity implements OnItemSelectedListener, TextWatcher {

    private static final String THIS_FILE="EditFilter";
    private Long filterId;
    private Filter filter;
    private Button saveButton;
    private long accountId;
    private EditText replaceTextEditor;
    private Spinner actionSpinner;
    private EditText matchesTextEditor;
    //	private View matchesContainer;
    private View replaceContainer;
    private Spinner replaceSpinner;
    private Spinner matcherSpinner;
    private boolean initMatcherSpinner;
    private boolean initReplaceSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Get back the concerned account and if any set the current (if not a new account is created)
        Intent intent=getIntent();
        filterId=intent.getLongExtra(Intent.EXTRA_UID, -1);
        /*accountId=intent.getLongExtra(Filter.FIELD_ACCOUNT, SipProfile.INVALID_ID);

        if (accountId==SipProfile.INVALID_ID) {
            WeiboLog.e(THIS_FILE, "Invalid account");
            finish();
        }

        filter=Filter.getFilterFromDbId(this, filterId, Filter.FULL_PROJ);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_filter);

        // Bind view objects
        actionSpinner=(Spinner) findViewById(R.id.filter_action);
        matcherSpinner=(Spinner) findViewById(R.id.matcher_type);
        replaceSpinner=(Spinner) findViewById(R.id.replace_type);

        replaceTextEditor=(EditText) findViewById(R.id.filter_replace);
        matchesTextEditor=(EditText) findViewById(R.id.filter_matches);

        //Bind containers objects
//		matchesContainer = (View) findViewById(R.id.matcher_block);
        replaceContainer=(View) findViewById(R.id.replace_block);

        actionSpinner.setOnItemSelectedListener(this);
        matcherSpinner.setOnItemSelectedListener(this);
        initMatcherSpinner=false;
        replaceSpinner.setOnItemSelectedListener(this);
        initReplaceSpinner=false;
        matchesTextEditor.addTextChangedListener(this);
        replaceTextEditor.addTextChangedListener(this);

        // Bind buttons to their actions
        Button bt=(Button) findViewById(R.id.cancel_bt);
        bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : clean prefs
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }
        });

        saveButton=(Button) findViewById(R.id.save_bt);*/
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFilter();
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
        fillLayout();
        checkFormValidity();
    }

    private void saveFilter() {
        //Update filter object

        filter.account=(int) accountId;
        filter.action=Filter.getActionForPosition(actionSpinner.getSelectedItemPosition());
        Filter.RegExpRepresentation repr=new Filter.RegExpRepresentation();
        //Matcher
        repr.type=Filter.getMatcherForPosition(matcherSpinner.getSelectedItemPosition());
        repr.fieldContent=matchesTextEditor.getText().toString();
        filter.setMatcherRepresentation(repr);

        //Rewriter
        if (filter.action==Filter.ACTION_REPLACE) {
            repr.fieldContent=replaceTextEditor.getText().toString();
            repr.type=Filter.getReplaceForPosition(replaceSpinner.getSelectedItemPosition());
            filter.setReplaceRepresentation(repr);
        } else if (filter.action==Filter.ACTION_AUTO_ANSWER) {
            filter.replacePattern=replaceTextEditor.getText().toString();
        } else {
            filter.replacePattern="";
        }

        //Save
        /*if (filterId<0) {
            Cursor currentCursor=getContentResolver().query(SipManager.FILTER_URI, new String[]{Filter._ID},
                Filter.FIELD_ACCOUNT+"=?",
                new String[]{
                    filter.account.toString()
                }, null);
            filter.priority=0;
            if (currentCursor!=null) {
                filter.priority=currentCursor.getCount();
                currentCursor.close();
            }
            getContentResolver().insert(SipManager.FILTER_URI, filter.getDbContentValues());
        } else {
            getContentResolver().update(ContentUris.withAppendedId(SipManager.FILTER_ID_URI_BASE, filterId), filter.getDbContentValues(), null, null);
        }*/
    }

    private void fillLayout() {
        //Set action
        actionSpinner.setSelection(Filter.getPositionForAction(filter.action));
        Filter.RegExpRepresentation repr=filter.getRepresentationForMatcher();
        //Set matcher - selection must be done first since raise on item change listener
        matcherSpinner.setSelection(Filter.getPositionForMatcher(repr.type));
        matchesTextEditor.setText(repr.fieldContent);
        //Set replace
        repr=filter.getRepresentationForReplace();
        replaceSpinner.setSelection(Filter.getPositionForReplace(repr.type));
        replaceTextEditor.setText(repr.fieldContent);

    }

    private void checkFormValidity() {
        boolean isValid=true;
        int action=Filter.getActionForPosition(actionSpinner.getSelectedItemPosition());

        if (TextUtils.isEmpty(matchesTextEditor.getText().toString())&&
            matcherNeedsText()) {
            isValid=false;
        }
        if (action==Filter.ACTION_AUTO_ANSWER) {
            if (!TextUtils.isEmpty(replaceTextEditor.getText().toString())) {
                try {
                    Integer.parseInt(replaceTextEditor.getText().toString());
                } catch (NumberFormatException e) {
                    isValid=false;
                }
            }
        }

        saveButton.setEnabled(isValid);
    }

    @Override
    public void onItemSelected(AdapterView<?> spinner, View arg1, int arg2, long arg3) {
        int spinnerId=spinner.getId();
        /*if (spinnerId==R.id.filter_action) {
            int action=Filter.getActionForPosition(actionSpinner.getSelectedItemPosition());
            if (action==Filter.ACTION_REPLACE||action==Filter.ACTION_AUTO_ANSWER) {
                replaceContainer.setVisibility(View.VISIBLE);
                if (action==Filter.ACTION_REPLACE) {
                    replaceSpinner.setVisibility(View.VISIBLE);
                    replaceTextEditor.setHint("");
                } else {
                    replaceSpinner.setVisibility(View.GONE);
                    replaceTextEditor.setHint(R.string.optional_sip_code);
                }
            } else {
                replaceContainer.setVisibility(View.GONE);
            }
        } else if (spinnerId==R.id.matcher_type) {
            if (initMatcherSpinner) {
                matchesTextEditor.setText("");
            } else {
                initMatcherSpinner=true;
            }
        } else if (spinnerId==R.id.replace_type) {
            if (initReplaceSpinner) {
                replaceTextEditor.setText("");
            } else {
                initReplaceSpinner=true;
            }
        }*/

        matchesTextEditor.setVisibility(matcherNeedsText() ? View.VISIBLE : View.GONE);
        checkFormValidity();
    }

    private boolean matcherNeedsText() {
        return Filter.getMatcherForPosition(matcherSpinner.getSelectedItemPosition())!=Filter.MATCHER_ALL&&
            Filter.getMatcherForPosition(matcherSpinner.getSelectedItemPosition())!=Filter.MATCHER_BLUETOOTH;
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        checkFormValidity();
    }

    @Override
    public void afterTextChanged(Editable s) {
        // Nothing to do
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Nothing to do

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkFormValidity();

    }

}
