package com.xiaoqiang.mypullrefresh;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnRefreshListener, OnClickListener  
{
	private AbsListView alv;  
    private PullRefreshLayout refreshLayout;  
    private View loading;  
    private RotateAnimation loadingAnimation;  
    private TextView loadTextView;  
    private MyAdapter adapter;  
    private boolean isLoading = false;    
    @Override  
    protected void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        init();  
    }  
    
    private void init()  
    {  
        alv = (AbsListView) findViewById(R.id.content_view);  
        refreshLayout = (PullRefreshLayout) findViewById(R.id.refresh_view);  
        refreshLayout.setOnRefreshListener(this);  
        initGridView();  
        //添加旋转动画
        loadingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(this, R.anim.rotating);  
        // 添加匀速转动动画  
        LinearInterpolator lir = new LinearInterpolator();  
        loadingAnimation.setInterpolator(lir);  
    } 
    /** 
     * ListView初始化方法 
     */  
    private void initListView()  
    {  
        List<String> items = new ArrayList<String>();  
        for (int i = 0; i < 30; i++)  
        {  
            items.add("这里是item " + i);  
        }  
        // 添加head  
        View headView = getLayoutInflater().inflate(R.layout.listview_head, null);  
        ((ListView) alv).addHeaderView(headView, null, false);  
        // 添加footer  
        View footerView = getLayoutInflater().inflate(R.layout.load_more, null);  
        loading = footerView.findViewById(R.id.loading_icon);  
        loadTextView = (TextView) footerView.findViewById(R.id.loadmore_tv);  
        ((ListView) alv).addFooterView(footerView, null, false);  
        footerView.setOnClickListener(this);  
        adapter = new MyAdapter(this, items);  
        alv.setAdapter(adapter);  
        alv.setOnItemLongClickListener(new OnItemLongClickListener()  
        {  
  
            @Override  
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)  
            {  
                Toast.makeText(MainActivity.this, "LongClick on " + parent.getAdapter().getItemId(position), Toast.LENGTH_SHORT).show();  
                return true;  
            }  
        });  
        alv.setOnItemClickListener(new OnItemClickListener()  
        {  
  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  
            {  
                Toast.makeText(MainActivity.this, " Click on " + parent.getAdapter().getItemId(position), Toast.LENGTH_SHORT).show();  
            }  
        });  
    }  
    /** 
     * GridView初始化方法 
     */  
    private void initGridView()  
    {  
        List<String> items = new ArrayList<String>();  
        for (int i = 0; i < 30; i++)  
        {  
            items.add("这里是item " + i);  
        }  
        adapter = new MyAdapter(this, items);  
        alv.setAdapter(adapter);  
        alv.setOnItemLongClickListener(new OnItemLongClickListener()  
        {  
  
            @Override  
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)  
            {  
                Toast.makeText(MainActivity.this, "LongClick on " + parent.getAdapter().getItemId(position), Toast.LENGTH_SHORT).show();  
                return true;  
            }  
        });  
        alv.setOnItemClickListener(new OnItemClickListener()  
        {  
  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  
            {  
                Toast.makeText(MainActivity.this, " Click on " + parent.getAdapter().getItemId(position), Toast.LENGTH_SHORT).show();  
            }  
        });  
    }  
    /** 
     * ExpandableListView初始化方法 
     */  
    private void initExpandableListView()  
    {  
        ((ExpandableListView) alv).setAdapter(new ExpandableListAdapter(this));  
        ((ExpandableListView) alv).setOnChildClickListener(new OnChildClickListener()  
        {  
  
            @Override  
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)  
            {  
                Toast.makeText(MainActivity.this, " Click on group " + groupPosition + " item " + childPosition, Toast.LENGTH_SHORT).show();  
                return true;  
            }  
        });  
        ((ExpandableListView) alv).setOnItemLongClickListener(new OnItemLongClickListener()  
        {  
  
            @Override  
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)  
            {  
                Toast.makeText(MainActivity.this, "LongClick on " + parent.getAdapter().getItemId(position), Toast.LENGTH_SHORT).show();  
                return true;  
            }  
        });  
        ((ExpandableListView) alv).setOnGroupClickListener(new OnGroupClickListener()  
        {  
  
            @Override  
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id)  
            {  
                if (parent.isGroupExpanded(groupPosition))  
                {  
                    // 如果展开则关闭  
                    parent.collapseGroup(groupPosition);  
                } else  
                {  
                    // 如果关闭则打开，注意这里是手动打开不要默认滚动否则会有bug  
                    parent.expandGroup(groupPosition);  
                }  
                return true;  
            }  
        });  
    }  
  
    
    //什么时候调用？？
	@Override
	public void onClick(View v) {
		   switch (v.getId())  
	        {  
	        case R.id.loadmore_layout:  
	            if (!isLoading)  
	            {  
	            	Toast.makeText(MainActivity.this, "什么时候触发 " , Toast.LENGTH_LONG).show();  
	                loading.setVisibility(View.VISIBLE);  
	                loading.startAnimation(loadingAnimation);  
	                loadTextView.setText("loading...");  
	                isLoading = true;  
	            }  
	            break;  
	        default:  
	            break;  
	        } 
	}

	@Override
	public void onRefresh() {
	    // 下拉刷新操作  
        new Handler()  
        {  
            @Override  
            public void handleMessage(Message msg)  
            {  
                refreshLayout.refreshFinish(PullRefreshLayout.REFRESH_SUCCEED);  
            }  
        }.sendEmptyMessageDelayed(0, 5000);  
		
	}   
	 class ExpandableListAdapter extends BaseExpandableListAdapter  {
		 private String[] groupsStrings;// = new String[] { "这里是group 0",  
         // "这里是group 1", "这里是group 2" };  
        private String[][] groupItems;  
        private Context context;  

        public ExpandableListAdapter(Context context)  
        {  
        this.context = context;  
        groupsStrings = new String[8];  
        for (int i = 0; i < groupsStrings.length; i++)  
        {  
        groupsStrings[i] = new String("这里是group " + i);  
        }  
        groupItems = new String[8][8];  
        for (int i = 0; i < groupItems.length; i++)  
        for (int j = 0; j < groupItems[i].length; j++)  
        {  
        groupItems[i][j] = new String("这里是group " + i + "里的item " + j);  
        }  
        } 

		@Override
		public int getGroupCount() {
			// TODO 自动生成的方法存根
			return groupsStrings.length; 
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO 自动生成的方法存根
			return groupItems[groupPosition].length;  
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO 自动生成的方法存根
		       return groupsStrings[groupPosition]; 
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO 自动生成的方法存根
		       return groupItems[groupPosition][childPosition];  
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO 自动生成的方法存根
			 return groupPosition;  
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO 自动生成的方法存根
			  return childPosition;  
		}

		@Override
		public boolean hasStableIds() {
			// TODO 自动生成的方法存根
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, null);  
            TextView tv = (TextView) view.findViewById(R.id.name_tv);  
            tv.setText(groupsStrings[groupPosition]);  
            return view; 
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			 View view = LayoutInflater.from(context).inflate(R.layout.list_item_layout, null);  
	            TextView tv = (TextView) view.findViewById(R.id.name_tv);  
	            tv.setText(groupItems[groupPosition][childPosition]);  
	            return view;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO 自动生成的方法存根
			return true;
		}
		 
	 }

	

}
