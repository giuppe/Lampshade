package eu.prismsw.lampshade;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import eu.prismsw.lampshade.database.ProviderHelper;
import eu.prismsw.lampshade.fragments.ArticleFragment;
import eu.prismsw.lampshade.fragments.IndexFragment;
import eu.prismsw.lampshade.fragments.TropesFragment;
import eu.prismsw.lampshade.listeners.OnInteractionListener;
import eu.prismsw.lampshade.listeners.OnLoadListener;
import eu.prismsw.lampshade.listeners.OnRemoveListener;
import eu.prismsw.lampshade.listeners.OnSaveListener;
import eu.prismsw.lampshade.providers.ArticleProvider;
import eu.prismsw.tropeswrapper.TropesArticleInfo;
import eu.prismsw.tropeswrapper.TropesHelper;

/** Shows a single TvTropes article */
public class ArticleActivity extends BaseActivity implements OnLoadListener, OnInteractionListener, OnSaveListener, OnRemoveListener {
	static final int DIALOG_LOAD_FAILED = 2;
	
	TropesFragment fragment;

	// Information about the article, needs less memory than the full article
	TropesArticleInfo articleInfo;
	// The url that was passed to the activity
	Uri passedUrl;
	// Where we actually ended up
	Uri trueUrl;
	
	SaveActionMode saveActionMode;
	RemoveActionMode removeActionMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_activity);
		
		// Prepare the ActionBar
		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		
		// The ActionMode objects need only be created once and can then be reused
		// In fact they should only be created once because it prevents conflicts between multiple ActionModes
		this.saveActionMode = new SaveActionMode(this, ArticleProvider.SAVED_URI);
		this.removeActionMode = new RemoveActionMode(this, ArticleProvider.SAVED_URI);
		
		// Get the url we are supposed to load
		Uri data = getIntent().getData();
		if(data != null) {
			this.passedUrl = data;
			
			// Check if the page is supposed to be loaded as an article
			// If this is set to true, we don't even check if it could be an index
			Boolean loadAsArticle = false;
			Bundle extras = getIntent().getExtras();
			if(extras != null) {
				loadAsArticle = getIntent().getExtras().getBoolean(TropesApplication.loadAsArticle);
			}
			
			if(savedInstanceState == null) {
				// If loadAsArticle is false and it is an index page, we create an IndexFragment
				// Otherwise we simply create an ArticleFragment
				if(!loadAsArticle && TropesHelper.isIndex(TropesHelper.titleFromUrl(data))) {
					this.fragment = IndexFragment.newInstance(this.passedUrl);
					
					getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
				}
				else {
					this.fragment = ArticleFragment.newInstance(this.passedUrl);
					
					getFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
                }
			}
		}
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	return super.onPrepareOptionsMenu(menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    

    @Override
    public Dialog onCreateDialog(int id) {
    	return onCreateDialog(id, null);
    }
    
    @Override
    public Dialog onCreateDialog(int id, Bundle args) {
    	Dialog dialog;
   		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	switch(id) {
    	default:
    		dialog = null;
    	}
    	return dialog;
    }
    

	public void onLinkSelected(Uri url) {
		if(ProviderHelper.articleExists(getContentResolver(), ArticleProvider.SAVED_URI, url)) {
			this.removeActionMode.startActionMode(url);
		}
		else {
			this.saveActionMode.startActionMode(url);
		}
	}

	public void onLoadError() {
        finish();
	}

	public void onLinkClicked(Uri url) {
        loadPage(url);
	}

	public void onLoadStart() {
	}

	public void onLoadFinish(Object result) {
		TropesArticleInfo info = (TropesArticleInfo) result;
		this.articleInfo = info;
		this.trueUrl = info.url;
		
		getActionBar().setTitle(info.title);
	}

	@Override
	public void onRemoveFinish(int affected) {
		invalidateOptionsMenu();
	}

	@Override
	public void onSaveFinish(Uri url) {
		invalidateOptionsMenu();
	}
}
