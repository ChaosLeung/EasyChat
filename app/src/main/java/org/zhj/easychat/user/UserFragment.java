package org.zhj.easychat.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.R;
import org.zhj.easychat.chat.ChatActivity;
import org.zhj.easychat.database.User;
import org.zhj.easychat.leancloud.LeanCloudUser;

/**
 * @author Chaos
 *         2015/03/11.
 */
public class UserFragment extends Fragment implements View.OnClickListener {

    private EditText nicknameText;
    private EditText introductionText;
    private EditText genderText;
    private EditText areaText;
    private EditText interestText;

    private ImageView avatarImage;

    private Button chatButton;
    private Button modifyPswButton;

    private boolean isEditing = false;
    private boolean isSaving = false;

    private LeanCloudUser mUser;
    private String userId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments() != null && getArguments().containsKey("UserId")) {
            userId = getArguments().getString("UserId");
        } else {
            userId = AVUser.getCurrentUser().getObjectId();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        nicknameText = (EditText) rootView.findViewById(R.id.nickname);
        introductionText = (EditText) rootView.findViewById(R.id.introduction);
        genderText = (EditText) rootView.findViewById(R.id.gender);
        areaText = (EditText) rootView.findViewById(R.id.area);
        interestText = (EditText) rootView.findViewById(R.id.interest);
        avatarImage = (ImageView) rootView.findViewById(R.id.avatar);
        chatButton = (Button) rootView.findViewById(R.id.chat);
        modifyPswButton = (Button) rootView.findViewById(R.id.modify_psw);

        fetchUserData();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (AVUser.getCurrentUser() != null && userId.equals(AVUser.getCurrentUser().getObjectId())) {
            getActivity().getMenuInflater().inflate(R.menu.menu_user_info, menu);
            menu.findItem(R.id.exit).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem edit = menu.findItem(R.id.edit);
        MenuItem save = menu.findItem(R.id.save);
        if (edit != null) {
            edit.setVisible(!isEditing);
        }
        if (save != null) {
            save.setVisible(isEditing);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                setEditing(true);
                break;
            case R.id.save:
                if (!isSaving) {
                    save();
                }
                break;
            case R.id.exit:
                AVUser.logOut();
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditing(boolean editing) {
        isEditing = editing;
        getActivity().invalidateOptionsMenu();
        setEnable(editing);
    }

    private void setEnable(boolean enable) {
        nicknameText.setEnabled(enable);
        introductionText.setEnabled(enable);
        genderText.setEnabled(enable);
        areaText.setEnabled(enable);
        interestText.setEnabled(enable);
        avatarImage.setClickable(enable);
    }

    private void save() {
        if (mUser != null) {
            isSaving = true;
            String nickname = nicknameText.getText().toString().trim();
            String introduction = introductionText.getText().toString().trim();
            String gender = genderText.getText().toString().trim();
            String area = areaText.getText().toString().trim();
            String interest = interestText.getText().toString().trim();

            mUser.setNickname(nickname);
            mUser.setIntroduction(introduction);
            mUser.setGender(gender);
            mUser.setArea(area);
            mUser.setInterest(interest);

            mUser.setFetchWhenSave(true);
            mUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    isSaving = false;
                    if (e == null) {
                        setEditing(false);
                    }
                    Toast.makeText(getActivity(), e == null ? "修改成功" : "修改失败", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat:
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("UserId", userId);
                intent.putExtra("Nickname", mUser.getNickname());
                startActivity(intent);
                break;
            case R.id.avatar:
                break;
            case R.id.modify_psw:
                startActivity(new Intent(getActivity(),ModifyPswActivity.class));
                break;
        }
    }

    private void setupUserInfo(LeanCloudUser user) {
        nicknameText.setText(user.getNickname());
        introductionText.setText(user.getIntroduction());
        interestText.setText(user.getInterest());
        genderText.setText(user.getGender());
        areaText.setText(user.getArea());

        Picasso.with(getActivity())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.default_avatar_blue)
                .error(R.drawable.default_avatar_blue)
                .resize(288, 288)
                .centerCrop()
                .into(avatarImage);
    }

    private void fetchUserData() {
        mUser = LeanCloudUser.getCurrentUser2();
        if (mUser != null) {
            if (userId.equals(mUser.getObjectId())) {
                setupUserInfo(LeanCloudUser.getCurrentUser2());
                modifyPswButton.setVisibility(View.VISIBLE);
                modifyPswButton.setOnClickListener(this);
                return;
            } else {
                //todo 检测是否互相关注
                chatButton.setText(R.string.chat);
            }
        }
        chatButton.setOnClickListener(this);
        AVQuery<LeanCloudUser> query = AVUser.getUserQuery(LeanCloudUser.class);
        query.whereEqualTo("objectId", userId);
        query.getFirstInBackground(new GetCallback<LeanCloudUser>() {
            @Override
            public void done(LeanCloudUser user, AVException e) {
                mUser = user;
                if (user != null && e == null) {
                    chatButton.setClickable(true);
                    setupUserInfo(user);
                } else {
                    Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
