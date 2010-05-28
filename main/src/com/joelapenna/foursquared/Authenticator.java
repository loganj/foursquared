/**
 * 
 */
package com.joelapenna.foursquared;

import static com.joelapenna.foursquared.FoursquaredSettings.DEBUG;

import com.joelapenna.foursquare.Foursquare;
import com.joelapenna.foursquared.location.LocationUtils;
import com.joelapenna.foursquared.preferences.Preferences;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

final class Authenticator extends AbstractAccountAuthenticator {
    private static final String TAG = "AuthenticatorImpl";
    final private Context mContext;
    final private Foursquared mFoursquared;
    
    Authenticator(Context context, Foursquared foursquared) {
        super(context);
        mContext = context;
        mFoursquared = foursquared;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        if ( DEBUG ) Log.d(TAG, "addAccount()");
        Log.i(TAG, "uid is " + mContext.getApplicationInfo().uid);
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.PARAM_LAUNCHMAIN, false);
        intent.putExtra(LoginActivity.PARAM_SETAUTHTOKEN, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle reply = new Bundle();
        reply.putParcelable(AccountManager.KEY_INTENT, intent);
        return reply;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
            Bundle options) {
        Log.i(TAG, "confirmCredentials()");
        
        // if AccountManager gave us a password, check it against the server
        if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            final boolean verified = confirmPasswordWithServer(account.name, password);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        
        // if AccountManager did not give us a password, we need to launch the LoginActivity
        // so the user can log in
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        //intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(LoginActivity.PARAM_LAUNCHMAIN, false);
        intent.putExtra(LoginActivity.PARAM_CONFIRMCREDENTIALS, true);
        final Bundle reply = new Bundle();
        reply.putParcelable(AccountManager.KEY_INTENT, intent);
        return reply;
    }
    
    
    private boolean confirmPasswordWithServer(String phoneNumber, String password)  {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = prefs.edit();
        Foursquare.Location location = LocationUtils.createFoursquareLocation(mFoursquared.getLastKnownLocation());
        try {
            return Preferences.loginUser(mFoursquared.getFoursquare(), phoneNumber, password, location, editor);
        } catch (Exception e) {
            // TODO: this is not great; the user will not see until LoginActivity fails that this is a network problem
            Log.w(TAG, "exception while attempting to verify password with server", e);
            return false;
        }
    }
    
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        Log.i(TAG, "editProperties()");
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
            Bundle options) throws NetworkErrorException {
        Log.i(TAG, "getAuthToken()");
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        
        if (password != null) {
            boolean confirmed = confirmPasswordWithServer(account.name, password);
            if (confirmed) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, AuthenticatorService.ACCOUNT_TYPE);
                result.putString(AccountManager.KEY_AUTHTOKEN, password);
                return result;
            }
        }
        
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.PARAM_LAUNCHMAIN, false);
        intent.putExtra(LoginActivity.PARAM_PHONENUMBER, account.name);
        intent.putExtra(LoginActivity.PARAM_SETAUTHTOKEN, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle reply = new Bundle();
        reply.putParcelable(AccountManager.KEY_INTENT, intent);
        return reply;
           
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        Log.i(TAG, "getAuthTokenLabel()");
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
            throws NetworkErrorException {
        Log.i(TAG, "hasFeatures()");
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse arg0, Account arg1,
            String arg2, Bundle arg3) {
        Log.i(TAG, "updateCredentials()");
        return null;
    }
}