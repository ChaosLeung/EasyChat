package org.zhj.easychat.user;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.FollowCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.squareup.picasso.Picasso;

import org.zhj.easychat.LoginActivity;
import org.zhj.easychat.R;
import org.zhj.easychat.chat.ChatActivity;
import org.zhj.easychat.leancloud.LeanCloudUser;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private boolean isFriend = false;

    private LeanCloudUser mCurrentUser;
    private String currentPageUserId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments() != null && getArguments().containsKey("UserId")) {
            currentPageUserId = getArguments().getString("UserId");
        } else {
            currentPageUserId = AVUser.getCurrentUser().getObjectId();
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
        if (AVUser.getCurrentUser() != null && currentPageUserId.equals(AVUser.getCurrentUser().getObjectId())) {
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
                startActivity(new Intent(getActivity(), LoginActivity.class));
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
        if (enable) {
            avatarImage.setOnClickListener(this);
        } else {
            avatarImage.setOnClickListener(null);
        }
    }

    private void save() {
        if (mCurrentUser != null) {
            isSaving = true;
            String nickname = nicknameText.getText().toString().trim();
            String introduction = introductionText.getText().toString().trim();
            String gender = genderText.getText().toString().trim();
            String area = areaText.getText().toString().trim();
            String interest = interestText.getText().toString().trim();

            mCurrentUser.setNickname(nickname);
            mCurrentUser.setIntroduction(introduction);
            mCurrentUser.setGender(gender);
            mCurrentUser.setArea(area);
            mCurrentUser.setInterest(interest);

            mCurrentUser.setFetchWhenSave(true);
            mCurrentUser.saveInBackground(new SaveCallback() {
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
                if (isFriend) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("UserId", currentPageUserId);
                    intent.putExtra("Nickname", mCurrentUser.getNickname());
                    startActivity(intent);
                } else {
                    chatButton.setClickable(false);
                    AVUser.getCurrentUser().followInBackground(currentPageUserId, new FollowCallback() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            if (e == null || e.getCode() == AVException.DUPLICATE_VALUE) {
                                Toast.makeText(getActivity(), "添加好友请求成功", Toast.LENGTH_SHORT).show();
                                chatButton.setText("已发送请求");
                            }
                        }
                    });
                }
                break;
            case R.id.avatar:
                Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
                getAlbum.setType("image/*");
                startActivityForResult(getAlbum, 1);
                break;
            case R.id.modify_psw:
                startActivity(new Intent(getActivity(), ModifyPswActivity.class));
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
        mCurrentUser = LeanCloudUser.getCurrentUser2();
        if (mCurrentUser != null) {
            if (currentPageUserId.equals(mCurrentUser.getObjectId())) {
                setupUserInfo(LeanCloudUser.getCurrentUser2());
                modifyPswButton.setVisibility(View.VISIBLE);
                modifyPswButton.setOnClickListener(this);
                return;
            } else {
                String currentUserId = mCurrentUser.getObjectId();
                AVUser currentPageUser = new AVUser();
                currentPageUser.setObjectId(currentPageUserId);
                //关注
                AVQuery<AVUser> followeeQuery = AVUser.followeeQuery(currentUserId, AVUser.class);
                followeeQuery.whereEqualTo("followee", currentPageUser);
                followeeQuery.limit(1);
                //粉丝
                AVQuery<AVUser> followerQuery = AVUser.followerQuery(currentUserId, AVUser.class);
                followerQuery.whereEqualTo("follower", currentPageUser);
                followerQuery.whereMatchesKeyInQuery("follower", "followee", followeeQuery);

                followerQuery.findInBackground(new FindCallback<AVUser>() {
                    @Override
                    public void done(List<AVUser> list, AVException e) {
                        if (e == null && list != null && list.size() == 1) {
                            isFriend = true;
                            chatButton.setText(R.string.chat);
                        } else {
                            chatButton.setText("添加好友");
                        }
                        chatButton.setOnClickListener(UserFragment.this);
                    }
                });
            }
        }
        AVQuery<LeanCloudUser> query = AVUser.getUserQuery(LeanCloudUser.class);
        query.whereEqualTo("objectId", currentPageUserId);
        query.getFirstInBackground(new GetCallback<LeanCloudUser>() {
            @Override
            public void done(LeanCloudUser user, AVException e) {
                mCurrentUser = user;
                if (user != null && e == null) {
                    chatButton.setClickable(true);
                    setupUserInfo(user);
                } else {
                    Toast.makeText(getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 1) {
            try {
                final AVFile file = AVFile.withAbsoluteLocalPath(AVUser.getCurrentUser().getObjectId(), FileUtils.getPath(getActivity(), data.getData()));
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            mCurrentUser.setAvatar(file.getUrl());
                            mCurrentUser.setFetchWhenSave(true);
                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        Picasso.with(getActivity())
                                                .load(file.getUrl())
                                                .placeholder(R.drawable.default_avatar_blue)
                                                .error(R.drawable.default_avatar_blue)
                                                .resize(288, 288)
                                                .centerCrop()
                                                .into(avatarImage);
                                        Toast.makeText(getActivity(), "上传头像成功", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "上传头像失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getActivity(), "上传头像失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
