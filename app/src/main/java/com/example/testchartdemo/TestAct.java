package com.example.testchartdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

/**
 * Created by Jeff on 2016/9/13 20:30.
 */
public class TestAct extends Activity{


    private ListView lv;
    private Button send;
    private EditText inputEdit;
    private EMConversation convrsation;

    private String toChatUsername="abcabc";
    private int chatType = 1;

    private DataAdapter adapter;
    private List<EMMessage> msgList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv);
        send = (Button) findViewById(R.id.send);
        inputEdit = (EditText) findViewById(R.id.input_content);

        convrsation = EMClient.getInstance().chatManager().getConversation(toChatUsername,
                getConversationType(chatType), true);
        // 把此会话的未读数置为0
        convrsation.markAllMessagesAsRead();
        msgList = convrsation.getAllMessages();
        adapter=new DataAdapter(msgList,TestAct.this);
        lv.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputEdit.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {

                    return;
                }
                setMesaage(content);
            }
        });

        EMClient.getInstance().chatManager().addMessageListener(msgListener);

    }

    private void setMesaage(String content) {

        // 创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        // 如果是群聊，设置chattype，默认是单聊
        if (chatType == Constant.CHATTYPE_GROUP)
            message.setChatType(EMMessage.ChatType.GroupChat);
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        msgList.add(message);

        adapter.notifyDataSetChanged();
        if (msgList.size() > 0) {
            lv.setSelection(lv.getCount() - 1);
        }
        inputEdit.setText("");
        inputEdit.clearFocus();
    }

    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {

            for (EMMessage message : messages) {
                Log.d("test", "mes---"+message);
                String username = null;
                // 群组消息
                if (message.getChatType() == EMMessage.ChatType.GroupChat || message.getChatType() == EMMessage.ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                }
                msgList.addAll(messages);
//                DataAdapter a=new DataAdapter(msgList,TestAct.this);
                adapter.notifyDataSetChanged();
                lv.setAdapter(adapter);
                lv.setSelection(lv.getCount() - 1);
                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    Log.d("test", "--------------"+toChatUsername);
                    msgList.addAll(messages);
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                    lv.setSelection(lv.getCount() - 1);
                    if (msgList.size() > 0) {
                        inputEdit.setSelection(lv.getCount() - 1);

                    }

                }
            }

            // 收到消息
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> list) {

        }

        @Override
        public void onMessageChanged(EMMessage emMessage, Object o) {

        }

    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
    }

    private class DataAdapter extends BaseAdapter {

        private List<EMMessage> msgs;
        private Context context;
        private LayoutInflater inflater;

        public DataAdapter(List<EMMessage> msgs, Context context_) {
            this.msgs = msgs;
            this.context = context_;
            inflater = LayoutInflater.from(context);
            Log.d("DataAdapter", "--------msgs------"+msgs.size());
        }

        @Override
        public int getCount() {
            return msgs.size();
        }

        @Override
        public Object getItem(int position) {
            return msgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            EMMessage message = (EMMessage) getItem(position);
            return message.direct() == EMMessage.Direct.RECEIVE ? 0 : 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EMMessage message = (EMMessage) getItem(position);
            int viewType = getItemViewType(position);
            if (convertView == null) {
                if (viewType == 0) {
                    convertView = inflater.inflate(R.layout.item_message_received, parent, false);
                } else {
                    convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
                }
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                convertView.setTag(holder);
            }

            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            holder.tv.setText(txtBody.getMessage());
            return convertView;
        }

        class ViewHolder {
            TextView tv;

        }
    }

    /**
     * 将应用的会话类型转化为SDK的会话类型
     *
     * @param chatType
     * @return
     */
    public static EMConversation.EMConversationType getConversationType(int chatType) {
        if (chatType == Constant.CHATTYPE_SINGLE) {
            return EMConversation.EMConversationType.Chat;
        } else if (chatType == Constant.CHATTYPE_GROUP) {
            return EMConversation.EMConversationType.GroupChat;
        } else {
            return EMConversation.EMConversationType.ChatRoom;
        }
    }
}
