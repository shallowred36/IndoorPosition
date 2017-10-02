package com.davidkuo.indoorposition;

import android.content.Context;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProvider;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

/**
 * Created by shallow_red36 on 2017/02/01.
 */

public class CognitoAuth {
    private static final String userPoolId = "ap-northeast-1_vIUQ9FvhT";
    private static final String clientId = "52481dlbrgaggt9jjnqe0v54ov";
    private static final String clientSecret = "a21q64o1ek322m7dbmeg3bm39e4ragfnk9ud3e8caabi2k1pqmb";
    private static final Regions cognitoRegion = Regions.AP_NORTHEAST_1;

    private static CognitoUserPool userPool;

    public static void init(Context context) {
        if (userPool != null) return;
        if (userPool == null) {
            ClientConfiguration clientConfig = new ClientConfiguration();
            AmazonCognitoIdentityProvider cipClient = new AmazonCognitoIdentityProviderClient(new AnonymousAWSCredentials(), clientConfig);
            cipClient.setRegion(Region.getRegion(cognitoRegion));
            userPool = new CognitoUserPool(context, userPoolId, clientId, clientSecret, cipClient);
        }
    }

    public static CognitoUserPool returnPool() {
        return userPool;
    }
}
