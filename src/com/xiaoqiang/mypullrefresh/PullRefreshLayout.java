package com.xiaoqiang.mypullrefresh;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PullRefreshLayout extends RelativeLayout implements OnTouchListener{

	public static final String TAG="PullRefreshLayout";
	public static final  int PULL_TO_REFRESH=0;   // ����ˢ�� 
	public static final  int RELEASE_TO_REFRESH=1; // �ͷ�ˢ��  
	public static final int REFRESHING=2;    // ����ˢ��  
	public static final int down=3;
    private int state = PULL_TO_REFRESH;  
    private OnRefreshListener mListener;  
    public static final int REFRESH_SUCCEED = 0;  
    public static final int REFRESH_FAIL = 1;  
    private View headView;  	 // ����ͷ  
    private View contentView; // ����  
    private float downY, lastY; 
    public float moveDeltaY = 0;   // �����ľ���  
    private float refreshDist = 200;    // �ͷ�ˢ�µľ���    
    private Timer timer;  
    private MyTimerTask mTask;   
    public float MOVE_SPEED = 8;
    private boolean isLayout = false;  
    private boolean canPull = true;    // �Ƿ�������� 
    private boolean isTouchInRefreshing = false;       // ��ˢ�¹����л������� 
    private float radio = 2;  
    private RotateAnimation rotateAnimation;  
    private RotateAnimation refreshingAnimation;
    private View pullView;    // �����ļ�ͷ  
    private View refreshingView; // ����ˢ�µ�ͼ��  
    private View stateImageView; // ˢ�½��ͼ��  
    private TextView stateTextView;  
     
	public PullRefreshLayout(Context context) {
		super(context);
		initView(context); 
	}
    public PullRefreshLayout(Context context, AttributeSet attrs)  
    {  
        super(context, attrs);  
        initView(context);  
    }  
  
    public PullRefreshLayout(Context context, AttributeSet attrs, int defStyle)  
    {  
        super(context, attrs, defStyle);  
        initView(context);  
    }  
    public void setOnRefreshListener(OnRefreshListener listener)  
    {  
        mListener = listener;  
    }  
    
	Handler updateHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			 // �ص��ٶ�����������moveDeltaY���������  
        MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));
        if (state == REFRESHING && moveDeltaY <= refreshDist && !isTouchInRefreshing)  {
        	 // ����ˢ�£���û�������ƵĻ�����ͣ����ʾ"����ˢ��..."  
            moveDeltaY = refreshDist;  
            mTask.cancel();        	
        }
        if (canPull)  {
            moveDeltaY -= MOVE_SPEED; 
        }
        if (moveDeltaY <= 0)  
        {  
            // ����ɻص�  
            moveDeltaY = 0;  
            pullView.clearAnimation();  
            // ��������ͷʱ�п��ܻ���ˢ�£�ֻ�е�ǰ״̬��������ˢ��ʱ�Ÿı�״̬  
            if (state != REFRESHING)  
                changeState(PULL_TO_REFRESH);  
            mTask.cancel();  
        }
            // ˢ�²���,���Զ�����onLayout  
            requestLayout();  
         
		}		
	};
    
	private void initView(Context context){
		timer=new Timer();
		mTask=new MyTimerTask(updateHandler);
	  rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.reverse_anim);  
	  refreshingAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.rotating);  
	    // �������ת������  
	    LinearInterpolator lir = new LinearInterpolator();  
	    rotateAnimation.setInterpolator(lir);  
	    refreshingAnimation.setInterpolator(lir); 
	}
	private void hidHeade(){
		 if (mTask != null)  
	        {  
			 //Timer��cancel()������ֹͣTimer������cancel()֮�����߾ͻ������ϵ��
	            mTask.cancel();  
	            mTask = null;  
	        }  
	        mTask = new MyTimerTask(updateHandler);  
	        timer.schedule(mTask, 0, 5);  
	}
	  /** 
     * ���ˢ�²�������ʾˢ�½�� 
     */ 
	public void refreshFinish(int refreshResult){
		 refreshingView.clearAnimation();
		 refreshingView.setVisibility(View.GONE);  
		 switch (refreshResult){
		 case REFRESH_SUCCEED: 
	         // ˢ�³ɹ�  
	            stateImageView.setVisibility(View.VISIBLE);  
	            stateTextView.setText("�ҵĳɹ�");  
	            stateImageView.setBackgroundResource(R.drawable.ic_launcher);  
	            break;  
		 case REFRESH_FAIL:  
	            // ˢ��ʧ��  
	            stateImageView.setVisibility(View.VISIBLE);  
	            stateTextView.setText("�ҵ�ʧ��");  
	            stateImageView.setBackgroundResource(R.drawable.ic_launcher);  
	            break; 
		   default:  
	            break;  
	                      
		 }
	
	 // ˢ�½��ͣ��1��  
	new Handler(){
	      @Override  
          public void handleMessage(Message msg)  
          {  
              state = PULL_TO_REFRESH;    // ����ˢ��  
              hidHeade();  
          }  
      }.sendEmptyMessageDelayed(0, 1000);  
	}
	//�ı�״̬ 
	private void changeState(int to){
		state=to;
		switch(state){
	
		case PULL_TO_REFRESH:  
		    // ����ˢ��  
            stateImageView.setVisibility(View.GONE);  
            stateTextView.setText("����ˢ��");  
           
            pullView.clearAnimation();  
            pullView.setVisibility(View.VISIBLE);  
            break; 
		case RELEASE_TO_REFRESH:  
	            // �ͷ�ˢ��  
	         stateTextView.setText("�ͷ�ˢ��");   
	         pullView.startAnimation(rotateAnimation);  
	         break; 
		 case REFRESHING:  
	            // ����ˢ��  
	            pullView.clearAnimation();  
	            refreshingView.setVisibility(View.VISIBLE);  
	            pullView.setVisibility(View.INVISIBLE);  
	            refreshingView.startAnimation(refreshingAnimation);  
	            stateTextView.setText("����ˢ��");  
	            break;
		 default:  
	            break; 
		}
	}	
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {		
	     // ��һ��item�ɼ��һ���������  
        AbsListView alv = null;  
        try  
        {  
            alv = (AbsListView) v;  
        } catch (Exception e)  
        {  
            Log.d(TAG, e.getMessage());  
            return false;  
        }  
        if (alv.getCount() == 0)  
        {  
            // û��item��ʱ��Ҳ��������ˢ��  
            canPull = true;  
        } 
        else if (alv.getFirstVisiblePosition() == 0 && alv.getChildAt(0).getTop() >= 0)  
        {  
            // ����AbsListView�Ķ�����  
        	System.out.println("����AbsListView�Ķ�����  ");
            canPull = true;  
        }
        else {
            canPull = false;        
            }           
        return false;  
    }  
	/* 
     * ���� Javadoc���ɸ��ؼ������Ƿ�ַ��¼�����ֹ�¼���ͻ 
     */  	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked())
		{
		case MotionEvent.ACTION_DOWN:   
            downY = ev.getY();  
            lastY = downY;  
            if (mTask != null)  
            {  
                mTask.cancel();  
            }  
            /* 
             * �����ĵط�λ������ͷ���֣���������û�ж�����ͷ���¼���Ӧ����ʱ��������۷���һ��false���½��������¼����ٷַ������� 
             * �������ǲ��ܽ�������ַ���ֱ�ӷ���true 
             */  
            if (ev.getY() < moveDeltaY)  
                return true;                         
            break; 
		 case MotionEvent.ACTION_MOVE:  
		       // canPull���ֵ�ڵ���onTouch�л����ListView�Ƿ񻬵��������ı䣬��˼���Ƿ������  
	            if (canPull)  
	            {  
	                // ��ʵ�ʻ�����������С������������ĸо�  
	                //moveDeltaY = moveDeltaY + (ev.getY() - lastY) / radio; 
	            	moveDeltaY = moveDeltaY + (ev.getY() - lastY);
	                if (moveDeltaY < 0)  
                       moveDeltaY = 0;  
            	                
	                if (moveDeltaY > getMeasuredHeight())  
	                    moveDeltaY = getMeasuredHeight();  
	    
	                if (state == REFRESHING)  
	                {  
	                    // ����ˢ�µ�ʱ�����ƶ�  
	                    isTouchInRefreshing = true;                    
	                }  
	            }  
	            lastY = ev.getY();  
	            // ������������ı����  
	            radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));  
	            requestLayout();   // ˢ�²���,���Զ�����onLayout  
	            if (moveDeltaY <= refreshDist && state == RELEASE_TO_REFRESH)  
	            {  
	                // �����������û�ﵽˢ�µľ����ҵ�ǰ״̬���ͷ�ˢ�£��ı�״̬Ϊ����ˢ��  
	            	 // System.out.println("��������û�ﵽˢ�µľ����ҵ�ǰ״̬���ͷ�ˢ�£��ı�״̬Ϊ����ˢ��  ");
	                changeState(PULL_TO_REFRESH);  
	            }  
	            
	            if (moveDeltaY >= refreshDist && state == PULL_TO_REFRESH)  
	            {    // �����������ﵽˢ�µľ��뵫��ǰ״̬������ˢ�£��ı�״̬Ϊ�ͷ�ˢ��  
	           	  //System.out.println("��������ﵽˢ�µľ��뵫��ǰ״̬������ˢ�£��ı�״̬Ϊ�ͷ�ˢ��   ");
	                changeState(RELEASE_TO_REFRESH);  
	            }  
	            	            
	            if (moveDeltaY > 8)  
	            {  
	                // ��ֹ�����������󴥷������¼��͵���¼�  
	            	//System.out.println("��ֹ�����������󴥷������¼��͵���¼� ");
	                clearContentViewEvents();  
	            }  
	                        
	            if (moveDeltaY > 0)  
	            {  
	                // ���������������ӿؼ������¼�  
	            	//System.out.println("���������������ӿؼ������¼�   ");
	                return true;  
	            }  
	            
	            break; 
		  case MotionEvent.ACTION_UP:  
		      if (moveDeltaY > refreshDist)  
	                // ����ˢ��ʱ�������ͷź�����ͷ������  
	                isTouchInRefreshing = false;  
	            if (state == RELEASE_TO_REFRESH)  
	            {  
	                changeState(REFRESHING);  
	                // ˢ�²���  
	                if (mListener != null)  
	                    mListener.onRefresh();  
	            } else  
	            {  
	  
	            }  
	            hidHeade();  
	            default:  
	            break;  
	        } 
		 // �¼��ַ���������  	
		return super.dispatchTouchEvent(ev);
	}
	 /** 
     * ͨ�������޸��ֶ�ȥ�������¼��͵���¼� 
     */ 
	private void clearContentViewEvents()  {
		try  
        {  
            Field[] fields = AbsListView.class.getDeclaredFields();  
            for (int i = 0; i < fields.length; i++)  
                if (fields[i].getName().equals("mPendingCheckForLongPress"))  
                {  
            // mPendingCheckForLongPress��AbsListView�е��ֶΣ�ͨ�������ȡ������Ϣ�б�ɾ����ȥ�������¼�  
                    fields[i].setAccessible(true);  
                    contentView.getHandler().removeCallbacks((Runnable) fields[i].get(contentView));  
                } else if (fields[i].getName().equals("mTouchMode"))  
                {  
                    // TOUCH_MODE_REST = -1�� �������ȥ������¼�  
                    fields[i].setAccessible(true);  
                    fields[i].set(contentView, -1);  
                }  
            // ȥ������  
            ((AbsListView) contentView).getSelector().setState(new int[]  
            { 0 });  
        } catch (Exception e)  
        {  
            Log.d(TAG, "error : " + e.toString());  
        }  
	}
	 /* 
     * ���� Javadoc��������ӰЧ������ɫֵ�����޸� 
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas) 
     */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	     if (moveDeltaY == 0)  
	            return;  
	        RectF rectF = new RectF(0, 0, getMeasuredWidth(), moveDeltaY);  
	        Paint paint = new Paint();  
	        paint.setAntiAlias(true);  
	        // ��Ӱ�ĸ߶�Ϊ26  
	        LinearGradient linearGradient = new LinearGradient(0, moveDeltaY, 0, moveDeltaY - 26, 0x66000000, 0x00000000, TileMode.CLAMP);  
	        paint.setShader(linearGradient);  
	        paint.setStyle(Style.FILL);  
	        // ��moveDeltaY�����ϱ䵭  
	        canvas.drawRect(rectF, paint); 
	}  
    private void initView()  
    {  
    	
        pullView = headView.findViewById(R.id.pull_icon);  
        stateTextView = (TextView) headView.findViewById(R.id.state_tv);  
        refreshingView = headView.findViewById(R.id.refreshing_icon);  
        stateImageView = headView.findViewById(R.id.state_iv);  
   
    }

    //��Ϊ��ǰViewGroup����Ԫ�ط���λ�úʹ�С ,��layout��������
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		 if (!isLayout)  
	        {  
	            // �����ǵ�һ�ν�����ʱ����һЩ��ʼ��  getChildAt����Ĳ������ǲ��������ε�����
	            headView = getChildAt(0);  
	            contentView = getChildAt(1);              
	            // ��AbsListView����OnTouchListener  
	            contentView.setOnTouchListener(this);  
	            isLayout = true;  
	            initView();  
	            refreshDist = ((ViewGroup) headView).getChildAt(0).getMeasuredHeight();  
	        }  	 
		if (canPull)  
	        {  
	   // �ı��ӿؼ��Ĳ���  ,ִ��headView�Ļ���
	  headView.layout(0, (int) moveDeltaY - headView.getMeasuredHeight(), headView.getMeasuredWidth(), (int) moveDeltaY);  
	  contentView.layout(0, (int) moveDeltaY, contentView.getMeasuredWidth(), (int) moveDeltaY + contentView.getMeasuredHeight());
	        }else super.onLayout(changed, l, t, r, b);  
	}  
	class MyTimerTask extends TimerTask  
    {  
        Handler handler;  
  
        public MyTimerTask(Handler handler)  
        {  
            this.handler = handler;  
        }  
  
        @Override  
        public void run()  
        {  
            handler.sendMessage(handler.obtainMessage());  
        }  
  
    } 
}
