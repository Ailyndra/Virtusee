/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtusee.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.virtusee.places.PlacesConstants;

/**
 * This Receiver class is used to listen for Broadcast Intents that announce
 * that a location change has occurred. This is used instead of a LocationListener
 * within an Activity is our only action is to start a service.
 */
public class LocationChangedReceiver extends BroadcastReceiver {
  
  protected static String TAG = "LocationChangedReceiver";
  
  /**
   * When a new location is received, extract it from the Intent and use
   * it to start the Service used to update the list of nearby places.
   * 
   * This is the Active receiver, used to receive Location updates when 
   * the Activity is visible. 
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    String locationKey = LocationManager.KEY_LOCATION_CHANGED;
    String providerEnabledKey = LocationManager.KEY_PROVIDER_ENABLED;
    if (intent.hasExtra(providerEnabledKey)) {
      if (!intent.getBooleanExtra(providerEnabledKey, true)) {
        Intent providerDisabledIntent = new Intent(PlacesConstants.ACTIVE_LOCATION_UPDATE_PROVIDER_DISABLED);
        context.sendBroadcast(providerDisabledIntent);    
      }
    }
    Log.e("GPS", "dapat");
    
    if (intent.hasExtra(locationKey)) {
//      Location location = (Location)intent.getExtras().get(locationKey);
      Log.e("GPS", "Actively Updating place list");
/*      Intent updateServiceIntent = new Intent(context, PlacesConstants.SUPPORTS_ECLAIR ? EclairPlacesUpdateService.class : PlacesUpdateService.class);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_LOCATION, location);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_RADIUS, PlacesConstants.DEFAULT_RADIUS);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_FORCEREFRESH, true);
      context.startService(updateServiceIntent);
      */
    }

  }
}