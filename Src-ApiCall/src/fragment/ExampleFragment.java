package com.example.fragment;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.example.R;
import com.example.adapter.ExampleAdapter;
import com.example.client.ApiCall;
import com.example.client.OnApiCallListener;
import com.example.client.RequestManager;
import com.example.client.ResponseStatus;
import com.example.client.entity.Message;
import com.example.client.request.ExampleRequest;
import com.example.client.response.ExampleResponse;
import com.example.client.response.Response;


public class ExampleFragment extends SherlockListFragment implements OnApiCallListener
{
	private final int LAZY_LOADING_TAKE = 3;
	private final int LAZY_LOADING_OFFSET = 1;
	
	private View mRootView;
	private View mFooterView;
	private ExampleAdapter mAdapter;
	private boolean mLazyLoading = false;
	private RequestManager mRequestManager = new RequestManager();
	
	private ArrayList<Message> mMessages = new ArrayList<Message>();

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{	
		mRootView = inflater.inflate(R.layout.layout_example, container, false);
		return mRootView;
	}
	
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		// load and show data
		if(RequestManager.isOnline(getActivity()))
		{
			loadData();
		}
		else
		{
			showOffline();
		}
	}
	
	
	@Override
	public void onPause()
	{
		// cancel async tasks
		mRequestManager.cancelAllRequests();

		// stop adapter
		if(mAdapter!=null) mAdapter.stop();
		
		super.onPause();
	}


	@Override
	public void onApiCallRespond(ApiCall call, ResponseStatus status, Response response)
	{
		if(response.getClass().getSimpleName().equalsIgnoreCase("ExampleResponse"))
		{
			ExampleResponse exampleResponse = (ExampleResponse) response;
			
			// error
			if(exampleResponse.isError())
			{
				Log.d("EXAMPLE", "onApiCallRespond: example response error");
				Log.d("EXAMPLE", "onApiCallRespond status code: " + status.getStatusCode());
				Log.d("EXAMPLE", "onApiCallRespond status message: " + status.getStatusMessage());
				Log.d("EXAMPLE", "onApiCallRespond error: " + exampleResponse.getErrorType() + ": " + exampleResponse.getErrorMessage());
			}
			
			// response
			else
			{
				Log.d("EXAMPLE", "onApiCallRespond: example response ok");
				Log.d("EXAMPLE", "onApiCallRespond status code: " + status.getStatusCode());
				Log.d("EXAMPLE", "onApiCallRespond status message: " + status.getStatusMessage());
				
				// data
				Iterator<Message> iterator = exampleResponse.getMessages().iterator();
				while(iterator.hasNext())
				{
					Message message = iterator.next();
					mMessages.add(new Message(message));
				}
				
				// show list container
				stopLazyLoadData();
				showList();
				
				// render or refresh view
				if(mAdapter==null) renderView();
				else mAdapter.notifyDataSetChanged();
			}
		}
		
		boolean finished = mRequestManager.finishRequest(call);
		Log.d("EXAMPLE", "finishRequest: " + finished);
	}


	@Override
	public void onApiCallFail(ApiCall call, ResponseStatus status, boolean parseFail)
	{
		if(call.getRequest().getClass().getSimpleName().equalsIgnoreCase("ExampleRequest"))
		{
			Log.d("EXAMPLE", "onApiCallFail: example request fail");
			Log.d("EXAMPLE", "onApiCallFail status code: " + status.getStatusCode());
			Log.d("EXAMPLE", "onApiCallFail status message: " + status.getStatusMessage());
			Log.d("EXAMPLE", "onApiCallFail parse fail: " + parseFail);
			
			// show list container
			stopLazyLoadData();
			showList();
			
			// render or refresh view
			if(mAdapter==null) renderView();
			else mAdapter.notifyDataSetChanged();
		}
		
		boolean finished = mRequestManager.finishRequest(call);
		Log.d("EXAMPLE", "finishRequest: " + finished);
	}
	
	
	private void loadData()
	{
		showProgress();
		
		// example request with paging
		ExampleRequest request = new ExampleRequest(0, LAZY_LOADING_TAKE);
		mRequestManager.executeRequest(request, this);
	}
	
	
	private void lazyLoadData()
	{
		startLazyLoadData();
		
		// example request with paging
		ExampleRequest request = new ExampleRequest(mMessages.size(), LAZY_LOADING_TAKE);
		mRequestManager.executeRequest(request, this);
	}
	
	
	private void startLazyLoadData()
	{
		mLazyLoading = true;

		// show footer
		ListView listView = getListView();
		listView.addFooterView(mFooterView);
	}
	
	
	private void stopLazyLoadData()
	{
		// hide footer
		ListView listView = getListView();
		listView.removeFooterView(mFooterView);
		
		mLazyLoading = false;
	}

	
	private void showList()
	{
		// show list container
		FrameLayout containerList = (FrameLayout) mRootView.findViewById(R.id.container_list);
		LinearLayout containerProgress = (LinearLayout) mRootView.findViewById(R.id.container_progress);
		LinearLayout containerOffline = (LinearLayout) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.VISIBLE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.GONE);
	}
	
	
	private void showProgress()
	{
		// show progress container
		FrameLayout containerList = (FrameLayout) mRootView.findViewById(R.id.container_list);
		LinearLayout containerProgress = (LinearLayout) mRootView.findViewById(R.id.container_progress);
		LinearLayout containerOffline = (LinearLayout) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.GONE);
		containerProgress.setVisibility(View.VISIBLE);
		containerOffline.setVisibility(View.GONE);
	}
	
	
	private void showOffline()
	{
		// show offline container
		FrameLayout containerList = (FrameLayout) mRootView.findViewById(R.id.container_list);
		LinearLayout containerProgress = (LinearLayout) mRootView.findViewById(R.id.container_progress);
		LinearLayout containerOffline = (LinearLayout) mRootView.findViewById(R.id.container_offline);
		containerList.setVisibility(View.GONE);
		containerProgress.setVisibility(View.GONE);
		containerOffline.setVisibility(View.VISIBLE);
	}

	
	private void renderView()
	{
		// TODO
	}
}
