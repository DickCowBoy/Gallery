/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.gallery3d.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.gallery3d.common.BlobCache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ReverseGeocoder {
    @SuppressWarnings("unused")
    private static final String TAG = "ReverseGeocoder";
    public static final int EARTH_RADIUS_METERS = 6378137;
    public static final int LAT_MIN = -90;
    public static final int LAT_MAX = 90;
    public static final int LON_MIN = -180;
    public static final int LON_MAX = 180;
    private static final int MAX_COUNTRY_NAME_LENGTH = 8;
    // If two points are within 20 miles of each other, use
    // "Around Palo Alto, CA" or "Around Mountain View, CA".
    // instead of directly jumping to the next level and saying
    // "California, US".
    private static final int MAX_LOCALITY_MILE_RANGE = 20;

    private static final String GEO_CACHE_FILE = "rev_geocoding";
    private static final int GEO_CACHE_MAX_ENTRIES = 1000;
    private static final int GEO_CACHE_MAX_BYTES = 500 * 1024;
    private static final int GEO_CACHE_VERSION = 0;

    private Context mContext;
    private Geocoder mGeocoder;
    private BlobCache mGeoCache;
    private ConnectivityManager mConnectivityManager;

    public ReverseGeocoder(Context context) {
        mContext = context;
        mGeocoder = new Geocoder(mContext);
        mGeoCache = CacheManager.getCache(context, GEO_CACHE_FILE,
                GEO_CACHE_MAX_ENTRIES, GEO_CACHE_MAX_BYTES,
                GEO_CACHE_VERSION);
        mConnectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public Address lookupAddress(final double latitude, final double longitude,
            boolean useCache) {
        try {
            long locationKey = (long) (((latitude + LAT_MAX) * 2 * LAT_MAX
                    + (longitude + LON_MAX)) * EARTH_RADIUS_METERS);
            byte[] cachedLocation = null;
            if (useCache && mGeoCache != null) {
                cachedLocation = mGeoCache.lookup(locationKey);
            }
            Address address = null;
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (cachedLocation == null || cachedLocation.length == 0) {
                if (networkInfo == null || !networkInfo.isConnected()) {
                    return null;
                }
                List<Address> addresses = mGeocoder.getFromLocation(latitude, longitude, 1);
                if (!addresses.isEmpty()) {
                    address = addresses.get(0);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(bos);
                    Locale locale = address.getLocale();
                    writeUTF(dos, locale.getLanguage());
                    writeUTF(dos, locale.getCountry());
                    writeUTF(dos, locale.getVariant());

                    writeUTF(dos, address.getThoroughfare());
                    int numAddressLines = address.getMaxAddressLineIndex();
                    dos.writeInt(numAddressLines);
                    for (int i = 0; i < numAddressLines; ++i) {
                        writeUTF(dos, address.getAddressLine(i));
                    }
                    writeUTF(dos, address.getFeatureName());
                    writeUTF(dos, address.getLocality());
                    writeUTF(dos, address.getAdminArea());
                    writeUTF(dos, address.getSubAdminArea());

                    writeUTF(dos, address.getCountryName());
                    writeUTF(dos, address.getCountryCode());
                    writeUTF(dos, address.getPostalCode());
                    writeUTF(dos, address.getPhone());
                    writeUTF(dos, address.getUrl());

                    dos.flush();
                    if (mGeoCache != null) {
                        mGeoCache.insert(locationKey, bos.toByteArray());
                    }
                    dos.close();
                }
            } else {
                // Parsing the address from the byte stream.
                DataInputStream dis = new DataInputStream(
                        new ByteArrayInputStream(cachedLocation));
                String language = readUTF(dis);
                String country = readUTF(dis);
                String variant = readUTF(dis);
                Locale locale = null;
                if (language != null) {
                    if (country == null) {
                        locale = new Locale(language);
                    } else if (variant == null) {
                        locale = new Locale(language, country);
                    } else {
                        locale = new Locale(language, country, variant);
                    }
                }
                if (!locale.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    dis.close();
                    return lookupAddress(latitude, longitude, false);
                }
                address = new Address(locale);

                address.setThoroughfare(readUTF(dis));
                int numAddressLines = dis.readInt();
                for (int i = 0; i < numAddressLines; ++i) {
                    address.setAddressLine(i, readUTF(dis));
                }
                address.setFeatureName(readUTF(dis));
                address.setLocality(readUTF(dis));
                address.setAdminArea(readUTF(dis));
                address.setSubAdminArea(readUTF(dis));

                address.setCountryName(readUTF(dis));
                address.setCountryCode(readUTF(dis));
                address.setPostalCode(readUTF(dis));
                address.setPhone(readUTF(dis));
                address.setUrl(readUTF(dis));
                dis.close();
            }
            return address;
        } catch (Exception e) {
            // Ignore.
        }
        return null;
    }

    public static final void writeUTF(DataOutputStream dos, String string) throws IOException {
        if (string == null) {
            dos.writeUTF("");
        } else {
            dos.writeUTF(string);
        }
    }

    public static final String readUTF(DataInputStream dis) throws IOException {
        String retVal = dis.readUTF();
        if (retVal.length() == 0)
            return null;
        return retVal;
    }
}
