package org.zhj.easychat.app;

import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * @author Chaos
 *         2015/02/24.
 */
public class BaseActionBarActivity extends ActionBarActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
