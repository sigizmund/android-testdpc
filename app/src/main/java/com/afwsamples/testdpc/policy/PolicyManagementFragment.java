/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.afwsamples.testdpc.policy;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.security.KeyChain;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.common.AppInfoArrayAdapter;
import com.afwsamples.testdpc.common.MediaDisplayFragment;
import com.afwsamples.testdpc.policy.accessibility.AccessibilityServiceInfoArrayAdapter;
import com.afwsamples.testdpc.policy.blockuninstallation.BlockUninstallationInfoArrayAdapter;
import com.afwsamples.testdpc.policy.certificate.DelegatedCertInstallerFragment;
import com.afwsamples.testdpc.policy.datausage.NetworkUsageStatsFragment;
import com.afwsamples.testdpc.policy.inputmethod.InputMethodInfoArrayAdapter;
import com.afwsamples.testdpc.policy.locktask.KioskModeActivity;
import com.afwsamples.testdpc.policy.locktask.LockTaskAppInfoArrayAdapter;
import com.afwsamples.testdpc.policy.systemupdatepolicy.SystemUpdatePolicyFragment;
import com.afwsamples.testdpc.policy.wifimanagement.WifiConfigCreationDialog;
import com.afwsamples.testdpc.policy.wifimanagement.WifiModificationFragment;
import com.afwsamples.testdpc.profilepolicy.ProfilePolicyManagementFragment;
import com.afwsamples.testdpc.profilepolicy.addsystemapps.EnableSystemAppsByIntentFragment;
import com.afwsamples.testdpc.profilepolicy.apprestrictions.ManageAppRestrictionsFragment;
import com.afwsamples.testdpc.profilepolicy.permission.ManageAppPermissionsFragment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;

/**
 * Provides several device management functions.
 * These include
 * 1) {@link DevicePolicyManager#setLockTaskPackages(android.content.ComponentName, String[])}
 * 2) {@link DevicePolicyManager#isLockTaskPermitted(String)}
 * 3) {@link UserManager#DISALLOW_DEBUGGING_FEATURES}
 * 4) {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES}
 * 5) {@link UserManager#DISALLOW_REMOVE_USER}
 * 6) {@link UserManager#DISALLOW_ADD_USER}
 * 7) {@link UserManager#DISALLOW_FACTORY_RESET}
 * 8) {@link UserManager#DISALLOW_CONFIG_CREDENTIALS}
 * 9) {@link UserManager#DISALLOW_SHARE_LOCATION}
 * 10) {@link UserManager#DISALLOW_CONFIG_TETHERING}
 * 11) {@link UserManager#DISALLOW_ADJUST_VOLUME}
 * 12) {@link UserManager#DISALLOW_UNMUTE_MICROPHONE}
 * 13) {@link UserManager#DISALLOW_MODIFY_ACCOUNTS}
 * 14) {@link UserManager#DISALLOW_SAFE_BOOT}
 * 15) {@link UserManager#DISALLOW_OUTGOING_BEAM}}
 * 16) {@link UserManager#DISALLOW_CREATE_WINDOWS}}
 * 17) {@link DevicePolicyManager#clearDeviceOwnerApp(String)}
 * 18) {@link DevicePolicyManager#getPermittedAccessibilityServices(android.content.ComponentName)}
 * 19) {@link DevicePolicyManager#getPermittedInputMethods(android.content.ComponentName)}
 * 20) {@link DevicePolicyManager#setAccountManagementDisabled(android.content.ComponentName,
 * String, boolean)}
 * 21) {@link DevicePolicyManager#getAccountTypesWithManagementDisabled()}
 * 22) {@link DevicePolicyManager#createAndInitializeUser(android.content.ComponentName, String,
 * String, android.content.ComponentName, android.os.Bundle)}
 * 23) {@link DevicePolicyManager#removeUser(android.content.ComponentName, android.os.UserHandle)}
 * 24) {@link DevicePolicyManager#setUninstallBlocked(android.content.ComponentName, String,
 * boolean)}
 * 25) {@link DevicePolicyManager#isUninstallBlocked(android.content.ComponentName, String)}
 * 26) {@link DevicePolicyManager#setCameraDisabled(android.content.ComponentName, boolean)}
 * 27) {@link DevicePolicyManager#getCameraDisabled(android.content.ComponentName)}
 * 28) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName,
 * android.content.Intent)}
 * 29) {@link DevicePolicyManager#enableSystemApp(android.content.ComponentName, String)}
 * 30) {@link DevicePolicyManager#setApplicationRestrictions(android.content.ComponentName, String,
 * android.os.Bundle)}
 * 31) {@link DevicePolicyManager#installKeyPair(android.content.ComponentName,
 * java.security.PrivateKey, java.security.cert.Certificate, String)}
 * 32) {@link DevicePolicyManager#installCaCert(android.content.ComponentName, byte[])}
 * 33) {@link DevicePolicyManager#uninstallAllUserCaCerts(android.content.ComponentName)}
 * 34) {@link DevicePolicyManager#getInstalledCaCerts(android.content.ComponentName)}
 * 35) {@link DevicePolicyManager#setStatusBarDisabled(ComponentName, boolean)}
 * 36) {@link DevicePolicyManager#setKeyguardDisabled(ComponentName, boolean)}
 * 37) {@link DevicePolicyManager#setPermissionPolicy(android.content.ComponentName, int)}
 * 38) {@link DevicePolicyManager#getPermissionPolicy(android.content.ComponentName)}
 * 39) {@link DevicePolicyManager#setPermissionGrantState(ComponentName, String, String, int)} (
 * android.content.ComponentName, String, String, boolean)}
 * 40) {@link DevicePolicyManager#setScreenCaptureDisabled(ComponentName, boolean)}
 * 41) {@link DevicePolicyManager#getScreenCaptureDisabled(ComponentName)}
 * 42) {@link DevicePolicyManager#setKeyguardDisabledFeatures(ComponentName, int)}
 * 43) {@link DevicePolicyManager#setMaximumTimeToLock(ComponentName, long)}
 * 44) {@link DevicePolicyManager#setMaximumFailedPasswordsForWipe(ComponentName, int)}
 * 45) {@link DevicePolicyManager#setApplicationHidden(ComponentName, String, boolean)}
 * 46) {@link UserManager#DISALLOW_CONFIG_WIFI}
 */
public class PolicyManagementFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final int INSTALL_KEY_CERTIFICATE_REQUEST_CODE = 7689;
    private static final int INSTALL_CA_CERTIFICATE_REQUEST_CODE = 7690;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 7691;
    private static final int CAPTURE_VIDEO_REQUEST_CODE = 7692;

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final String X509_CERT_TYPE = "X.509";
    public static final String TAG = "PolicyManagementFragment";

    public static final String OVERRIDE_KEY_SELECTION_KEY = "override_key_selection";

    private static final String BLOCK_UNINSTALLATION_BY_PKG_KEY = "block_uninstallation_by_pkg";
    private static final String BLOCK_UNINSTALLATION_LIST_KEY = "block_uninstallation_list";
    private static final String CAPTURE_IMAGE_KEY = "capture_image";
    private static final String CAPTURE_VIDEO_KEY = "capture_video";
    private static final String CHECK_LOCK_TASK_PERMITTED_KEY = "check_lock_task_permitted";
    private static final String CREATE_AND_INITIALIZE_USER_KEY = "create_and_initialize_user";
    private static final String DELEGATED_CERT_INSTALLER_KEY = "manage_cert_installer";
    private static final String DEVICE_OWNER_STATUS_KEY = "device_owner_status";
    private static final String DISABLE_CAMERA_KEY = "disable_camera";
    private static final String DISABLE_KEYGUARD = "disable_keyguard";
    private static final String DISABLE_SCREEN_CAPTURE_KEY = "disable_screen_capture";
    private static final String DISABLE_STATUS_BAR = "disable_status_bar";
    private static final String ENABLE_SYSTEM_APPS_BY_INTENT_KEY = "enable_system_apps_by_intent";
    private static final String ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY
            = "enable_system_apps_by_package_name";
    private static final String ENABLE_SYSTEM_APPS_KEY = "enable_system_apps";
    private static final String INSTALL_UPDATE_APP = "install_update_app";
    private static final String GET_CA_CERTIFICATES_KEY = "get_ca_certificates";
    private static final String GET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "get_disable_account_management";
    private static final String HIDE_APPS_KEY = "hide_apps";
    private static final String INSTALL_CA_CERTIFICATE_KEY = "install_ca_certificate";
    private static final String INSTALL_KEY_CERTIFICATE_KEY = "install_key_certificate";
    private static final String INSTALL_NONMARKET_APPS_KEY
            = "install_nonmarket_apps";
    private static final String KEYGUARD_DISABLE_FINGERPRINT = "keyguard_disable_fingerprint";
    private static final String KEYGUARD_DISABLE_SECURE_CAMERA = "keyguard_disable_secure_camera";
    private static final String KEYGUARD_DISABLE_SECURE_NOTIFICATIONS
            = "keyguard_disable_secure_notifications";
    private static final String KEYGUARD_DISABLE_TRUST_AGENTS = "keyguard_disable_trust_agents";
    private static final String KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS
            = "keyguard_disable_unredacted_notifications";
    private static final String KEYGUARD_DISABLE_WIDGETS = "keyguard_disable_widgets";
    private static final String KEYGUARD_PREFERENCES = "keyguard_preferences";
    private static final String KEY_MAX_FAILS_BEFORE_WIPE = "key_max_fails_before_wipe";
    private static final String KEY_MAX_TIME_SCREEN_LOCK = "key_max_time_screen_lock";
    private static final String MANAGE_APP_PERMISSIONS_KEY = "manage_app_permissions";
    private static final String MANAGE_APP_RESTRICTIONS_KEY = "manage_app_restrictions";
    private static final String MANAGED_PROFILE_SPECIFIC_POLICIES_KEY = "managed_profile_policies";
    private static final String MANAGE_LOCK_TASK_LIST_KEY = "manage_lock_task";
    private static final String NETWORK_STATS_KEY = "network_stats";
    private static final String REENABLE_KEYGUARD = "reenable_keyguard";
    private static final String REENABLE_STATUS_BAR = "reenable_status_bar";
    private static final String REMOVE_ALL_CERTIFICATES_KEY = "remove_all_ca_certificates";
    private static final String REMOVE_DEVICE_OWNER_KEY = "remove_device_owner";
    private static final String REMOVE_USER_KEY = "remove_user";
    private static final String SET_ACCESSIBILITY_SERVICES_KEY = "set_accessibility_services";
    private static final String SET_DISABLE_ACCOUNT_MANAGEMENT_KEY
            = "set_disable_account_management";
    private static final String SET_INPUT_METHODS_KEY = "set_input_methods";
    private static final String SET_PERMISSION_POLICY_KEY = "set_permission_policy";
    private static final String SET_USER_RESTRICTIONS_KEY = "set_user_restrictions";
    private static final String START_KIOSK_MODE = "start_kiosk_mode";
    private static final String START_LOCK_TASK = "start_lock_task";
    private static final String STAY_ON_WHILE_PLUGGED_IN = "stay_on_while_plugged_in";
    private static final String STOP_LOCK_TASK = "stop_lock_task";
    private static final String SYSTEM_UPDATE_POLICY_KEY = "system_update_policy";
    private static final String UNHIDE_APPS_KEY = "unhide_apps";
    private static final String WIPE_DATA_KEY = "wipe_data";
    private static final String CREATE_WIFI_CONFIGURATION_KEY = "create_wifi_configuration";
    private static final String WIFI_CONFIG_LOCKDOWN_ENABLE_KEY = "enable_wifi_config_lockdown";
    private static final String MODIFY_WIFI_CONFIGURATION_KEY = "modify_wifi_configuration";
    private static final String TAG_WIFI_CONFIG_CREATION = "wifi_config_creation";
    private static final String WIFI_CONFIG_LOCKDOWN_ON = "1";
    private static final String WIFI_CONFIG_LOCKDOWN_OFF = "0";

    private static final long MS_PER_SECOND = 1000;

    private static final String BATTERY_PLUGGED_ANY = Integer.toString(
            BatteryManager.BATTERY_PLUGGED_AC |
            BatteryManager.BATTERY_PLUGGED_USB |
            BatteryManager.BATTERY_PLUGGED_WIRELESS);
    private static final String DONT_STAY_ON = "0";

    private static final String[] PRIMARY_USER_ONLY_PREFERENCES = {
            WIPE_DATA_KEY, REMOVE_DEVICE_OWNER_KEY, CREATE_AND_INITIALIZE_USER_KEY, REMOVE_USER_KEY,
            MANAGE_LOCK_TASK_LIST_KEY, CHECK_LOCK_TASK_PERMITTED_KEY, START_LOCK_TASK,
            STOP_LOCK_TASK, DISABLE_STATUS_BAR, REENABLE_STATUS_BAR, DISABLE_KEYGUARD,
            REENABLE_KEYGUARD, START_KIOSK_MODE, SYSTEM_UPDATE_POLICY_KEY, KEYGUARD_DISABLE_WIDGETS,
            KEYGUARD_DISABLE_SECURE_CAMERA, KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
            STAY_ON_WHILE_PLUGGED_IN
    };

    private static String[] MNC_PLUS_PREFERENCES = {
            OVERRIDE_KEY_SELECTION_KEY, START_LOCK_TASK, STOP_LOCK_TASK, SYSTEM_UPDATE_POLICY_KEY,
            NETWORK_STATS_KEY, DELEGATED_CERT_INSTALLER_KEY, DISABLE_STATUS_BAR,
            REENABLE_STATUS_BAR, DISABLE_KEYGUARD, REENABLE_KEYGUARD, START_KIOSK_MODE,
            SET_PERMISSION_POLICY_KEY, MANAGE_APP_PERMISSIONS_KEY,STAY_ON_WHILE_PLUGGED_IN,
            WIFI_CONFIG_LOCKDOWN_ENABLE_KEY
    };

    /**
     * Preferences that are allowed only in MNC+ if it profile owner. This does not restrict device
     * owner.
     */
    private static String[] MANAGED_PROFILE_MNC_PLUS_PREFERENCES = {
            KEYGUARD_PREFERENCES
    };

    private static final String[] MANAGED_PROFILE_SPECIFIC_OPTIONS = {
            MANAGED_PROFILE_SPECIFIC_POLICIES_KEY
    };

    /**
     * Contains the list of preferences for keyguard features. Must have the same order and size
     * with {@link PolicyManagementFragment#KEYGUARD_DISABLE_FLAGS}.
     */
    private static final String[] KEYGUARD_DISABLE_PREFERENCES = {
            KEYGUARD_DISABLE_WIDGETS, KEYGUARD_DISABLE_SECURE_CAMERA,
            KEYGUARD_DISABLE_SECURE_NOTIFICATIONS, KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
            KEYGUARD_DISABLE_TRUST_AGENTS, KEYGUARD_DISABLE_FINGERPRINT
    };

    /**
     * Contains the list of DevicePolicyManager flags for keyguard features. Must have the same
     * order and size with {@link PolicyManagementFragment#KEYGUARD_DISABLE_PREFERENCES}.
     */
    private static int[] KEYGUARD_DISABLE_FLAGS = {
            DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL,
            DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA,
            DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
            DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
            DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS,
            DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT
    };

    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private String mPackageName;
    private ComponentName mAdminComponentName;
    private UserManager mUserManager;

    private SwitchPreference mDisableCameraSwitchPreference;
    private SwitchPreference mDisableScreenCaptureSwitchPreference;

    private SwitchPreference mStayOnWhilePluggedInSwitchPreference;
    private SwitchPreference mInstallNonMarketAppsPreference;

    private GetAccessibilityServicesTask mGetAccessibilityServicesTask = null;
    private GetInputMethodsTask mGetInputMethodsTask = null;
    private ShowCaCertificateListTask mShowCaCertificateListTask = null;

    private Uri mImageUri;
    private Uri mVideoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (KEYGUARD_DISABLE_PREFERENCES.length != KEYGUARD_DISABLE_FLAGS.length) {
            throw new IllegalStateException("KEYGUARD_DISABLE_PREFERENCES must have the same length"
                    + "as KEYGUARD_DISABLE_FLAGS");
        }

        mAdminComponentName = DeviceAdminReceiver.getComponentName(getActivity());
        mDevicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mUserManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        mPackageManager = getActivity().getPackageManager();
        mPackageName = getActivity().getPackageName();

        mImageUri = getStorageUri("image.jpg");
        mVideoUri = getStorageUri("video.mp4");

        addPreferencesFromResource(R.xml.device_policy_header);

        EditTextPreference overrideKeySelectionPreference =
                (EditTextPreference) findPreference(OVERRIDE_KEY_SELECTION_KEY);
        overrideKeySelectionPreference.setOnPreferenceChangeListener(this);
        overrideKeySelectionPreference.setSummary(overrideKeySelectionPreference.getText());
        findPreference(MANAGE_LOCK_TASK_LIST_KEY).setOnPreferenceClickListener(this);
        findPreference(CHECK_LOCK_TASK_PERMITTED_KEY).setOnPreferenceClickListener(this);
        findPreference(START_LOCK_TASK).setOnPreferenceClickListener(this);
        findPreference(STOP_LOCK_TASK).setOnPreferenceClickListener(this);
        findPreference(CREATE_AND_INITIALIZE_USER_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_USER_KEY).setOnPreferenceClickListener(this);
        mDisableCameraSwitchPreference = (SwitchPreference) findPreference(DISABLE_CAMERA_KEY);
        findPreference(CAPTURE_IMAGE_KEY).setOnPreferenceClickListener(this);
        findPreference(CAPTURE_VIDEO_KEY).setOnPreferenceClickListener(this);
        mDisableCameraSwitchPreference.setOnPreferenceChangeListener(this);
        mDisableScreenCaptureSwitchPreference = (SwitchPreference) findPreference(
                DISABLE_SCREEN_CAPTURE_KEY);
        mDisableScreenCaptureSwitchPreference.setOnPreferenceChangeListener(this);
        findPreference(SYSTEM_UPDATE_POLICY_KEY).setOnPreferenceClickListener(this);
        findPreference(NETWORK_STATS_KEY).setOnPreferenceClickListener(this);
        findPreference(DELEGATED_CERT_INSTALLER_KEY).setOnPreferenceClickListener(this);
        findPreference(DISABLE_STATUS_BAR).setOnPreferenceClickListener(this);
        findPreference(REENABLE_STATUS_BAR).setOnPreferenceClickListener(this);
        findPreference(DISABLE_KEYGUARD).setOnPreferenceClickListener(this);
        findPreference(REENABLE_KEYGUARD).setOnPreferenceClickListener(this);
        findPreference(START_KIOSK_MODE).setOnPreferenceClickListener(this);
        mStayOnWhilePluggedInSwitchPreference = (SwitchPreference) findPreference(
                STAY_ON_WHILE_PLUGGED_IN);
        mStayOnWhilePluggedInSwitchPreference.setOnPreferenceChangeListener(this);
        findPreference(WIPE_DATA_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_DEVICE_OWNER_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_ACCESSIBILITY_SERVICES_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_INPUT_METHODS_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_DISABLE_ACCOUNT_MANAGEMENT_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_BY_PKG_KEY).setOnPreferenceClickListener(this);
        findPreference(BLOCK_UNINSTALLATION_LIST_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY).setOnPreferenceClickListener(this);
        findPreference(ENABLE_SYSTEM_APPS_BY_INTENT_KEY).setOnPreferenceClickListener(this);
        findPreference(HIDE_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(UNHIDE_APPS_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGE_APP_RESTRICTIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_KEY_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_CA_CERTIFICATE_KEY).setOnPreferenceClickListener(this);
        findPreference(GET_CA_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(REMOVE_ALL_CERTIFICATES_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGED_PROFILE_SPECIFIC_POLICIES_KEY).setOnPreferenceClickListener(this);
        findPreference(SET_PERMISSION_POLICY_KEY).setOnPreferenceClickListener(this);
        findPreference(MANAGE_APP_PERMISSIONS_KEY).setOnPreferenceClickListener(this);
        findPreference(KEY_MAX_TIME_SCREEN_LOCK).setOnPreferenceChangeListener(this);
        findPreference(KEY_MAX_FAILS_BEFORE_WIPE).setOnPreferenceChangeListener(this);
        findPreference(CREATE_WIFI_CONFIGURATION_KEY).setOnPreferenceClickListener(this);
        findPreference(WIFI_CONFIG_LOCKDOWN_ENABLE_KEY).setOnPreferenceChangeListener(this);
        findPreference(MODIFY_WIFI_CONFIGURATION_KEY).setOnPreferenceClickListener(this);
        findPreference(INSTALL_UPDATE_APP).setOnPreferenceClickListener(this);
        mInstallNonMarketAppsPreference = (SwitchPreference) findPreference(
                INSTALL_NONMARKET_APPS_KEY);
        mInstallNonMarketAppsPreference.setOnPreferenceChangeListener(this);
        findPreference(SET_USER_RESTRICTIONS_KEY).setOnPreferenceClickListener(this);

        reloadCameraDisableUi();
        reloadScreenCaptureDisableUi();

        setPreferenceChangeListeners(KEYGUARD_DISABLE_PREFERENCES);
        updateKeyguardFeaturesUi();
        disableIncompatibleManagementOptionsInCurrentProfile();
        disableIncompatibleManagementOptionsByApiLevel();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.policies_management);

        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(mPackageName);
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(mPackageName);
        if (!isDeviceOwner && !isProfileOwner) {
            showToast(R.string.this_is_not_a_device_owner);
            getActivity().finish();
        }

        // The settings might get changed outside the device policy app,
        // so, we need to make sure the preference gets updated accordingly.
        updateStayOnWhilePluggedInPreference();
        updateInstallNonMarketAppsPreference();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case MANAGE_LOCK_TASK_LIST_KEY:
                showManageLockTaskListPrompt(R.string.lock_task_title,
                        new ManageLockTaskListCallback() {
                            @Override
                            public void onPositiveButtonClicked(String[] lockTaskArray) {
                                mDevicePolicyManager.setLockTaskPackages(
                                        DeviceAdminReceiver.getComponentName(getActivity()),
                                        lockTaskArray);
                            }
                        }
                );
                return true;
            case CHECK_LOCK_TASK_PERMITTED_KEY:
                showCheckLockTaskPermittedPrompt();
                return true;
            case START_LOCK_TASK:
                getActivity().startLockTask();
                return true;
            case STOP_LOCK_TASK:
                try {
                    getActivity().stopLockTask();
                } catch (IllegalStateException e) {
                    // no lock task present, ignore
                }
                return true;
            case WIPE_DATA_KEY:
                showWipeDataPrompt();
                return true;
            case REMOVE_DEVICE_OWNER_KEY:
                showRemoveDeviceOwnerPrompt();
                return true;
            case SET_ACCESSIBILITY_SERVICES_KEY:
                // Avoid starting the same task twice.
                if (mGetAccessibilityServicesTask != null && !mGetAccessibilityServicesTask
                        .isCancelled()) {
                    mGetAccessibilityServicesTask.cancel(true);
                }
                mGetAccessibilityServicesTask = new GetAccessibilityServicesTask();
                mGetAccessibilityServicesTask.execute();
                return true;
            case SET_INPUT_METHODS_KEY:
                // Avoid starting the same task twice.
                if (mGetInputMethodsTask != null && !mGetInputMethodsTask.isCancelled()) {
                    mGetInputMethodsTask.cancel(true);
                }
                mGetInputMethodsTask = new GetInputMethodsTask();
                mGetInputMethodsTask.execute();
                return true;
            case SET_DISABLE_ACCOUNT_MANAGEMENT_KEY:
                showSetDisableAccountManagementPrompt();
                return true;
            case GET_DISABLE_ACCOUNT_MANAGEMENT_KEY:
                showDisableAccountTypeList();
                return true;
            case CREATE_AND_INITIALIZE_USER_KEY:
                showCreateUserPrompt();
                return true;
            case REMOVE_USER_KEY:
                showRemoveUserPrompt();
                return true;
            case BLOCK_UNINSTALLATION_BY_PKG_KEY:
                showBlockUninstallationByPackageNamePrompt();
                return true;
            case BLOCK_UNINSTALLATION_LIST_KEY:
                showBlockUninstallationPrompt();
                return true;
            case ENABLE_SYSTEM_APPS_KEY:
                showEnableSystemAppsPrompt();
                return true;
            case ENABLE_SYSTEM_APPS_BY_PACKAGE_NAME_KEY:
                showEnableSystemAppByPackageNamePrompt();
                return true;
            case ENABLE_SYSTEM_APPS_BY_INTENT_KEY:
                showFragment(new EnableSystemAppsByIntentFragment());
                return true;
            case HIDE_APPS_KEY:
                showHideAppsPrompt(false);
                return true;
            case UNHIDE_APPS_KEY:
                showHideAppsPrompt(true);
                return true;
            case MANAGE_APP_RESTRICTIONS_KEY:
                showFragment(new ManageAppRestrictionsFragment());
                return true;
            case SET_PERMISSION_POLICY_KEY:
                showSetPermissionPolicyDialog();
                return true;
            case MANAGE_APP_PERMISSIONS_KEY:
                showFragment(new ManageAppPermissionsFragment());
                return true;
            case INSTALL_KEY_CERTIFICATE_KEY:
                showFileViewerForImportingCertificate(INSTALL_KEY_CERTIFICATE_REQUEST_CODE);
                return true;
            case INSTALL_CA_CERTIFICATE_KEY:
                showFileViewerForImportingCertificate(INSTALL_CA_CERTIFICATE_REQUEST_CODE);
                return true;
            case GET_CA_CERTIFICATES_KEY:
                showCaCertificateList();
                return true;
            case REMOVE_ALL_CERTIFICATES_KEY:
                mDevicePolicyManager.uninstallAllUserCaCerts(mAdminComponentName);
                showToast(R.string.all_ca_certificates_removed);
                return true;
            case MANAGED_PROFILE_SPECIFIC_POLICIES_KEY:
                showFragment(new ProfilePolicyManagementFragment());
                return true;
            case SYSTEM_UPDATE_POLICY_KEY:
                showFragment(new SystemUpdatePolicyFragment());
                return true;
            case NETWORK_STATS_KEY:
                showFragment(new NetworkUsageStatsFragment());
                return true;
            case DELEGATED_CERT_INSTALLER_KEY:
                showFragment(new DelegatedCertInstallerFragment());
                return true;
            case DISABLE_STATUS_BAR:
                if (!mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, true)) {
                    showToast("Unable to disable status bar when lock password is set.");
                }
                return true;
            case REENABLE_STATUS_BAR:
                mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, false);
                return true;
            case DISABLE_KEYGUARD:
                if (!mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, true)) {
                    // this should not happen
                    showToast("Unable to disable keyguard");
                }
                return true;
            case REENABLE_KEYGUARD:
                mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, false);
                return true;
            case START_KIOSK_MODE:
                showManageLockTaskListPrompt(R.string.kiosk_select_title,
                        new ManageLockTaskListCallback() {
                            @Override
                            public void onPositiveButtonClicked(String[] lockTaskArray) {
                                startKioskMode(lockTaskArray);
                            }
                        }
                );
                return true;
            case CAPTURE_IMAGE_KEY:
                dispatchCaptureIntent(MediaStore.ACTION_IMAGE_CAPTURE,
                        CAPTURE_IMAGE_REQUEST_CODE, mImageUri);
                return true;
            case CAPTURE_VIDEO_KEY:
                dispatchCaptureIntent(MediaStore.ACTION_VIDEO_CAPTURE,
                        CAPTURE_VIDEO_REQUEST_CODE, mVideoUri);
                return true;
            case CREATE_WIFI_CONFIGURATION_KEY:
                showWifiConfigCreationDialog();
                return true;
            case MODIFY_WIFI_CONFIGURATION_KEY:
                showFragment(new WifiModificationFragment());
                return true;
            case SET_USER_RESTRICTIONS_KEY:
                showFragment(new UserRestrictionsDisplayFragment());
                return true;
            case INSTALL_UPDATE_APP:
                showToast("Not implemented yet");
                // TODO(kirillov): add code here
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (Arrays.asList(KEYGUARD_DISABLE_PREFERENCES).contains(key)) {
            ((SwitchPreference) preference).setChecked((Boolean) newValue);
            updateKeyguardDisabledFeatures();
            return false;
        }

        switch (key) {
            case OVERRIDE_KEY_SELECTION_KEY:
                preference.setSummary((String) newValue);
                return true;
            case DISABLE_CAMERA_KEY:
                mDevicePolicyManager.setCameraDisabled(mAdminComponentName, (Boolean) newValue);
                // Reload UI to verify the camera is enable / disable correctly.
                reloadCameraDisableUi();
                return true;
            case DISABLE_SCREEN_CAPTURE_KEY:
                mDevicePolicyManager.setScreenCaptureDisabled(mAdminComponentName,
                        (Boolean) newValue);
                // Reload UI to verify that screen capture was enabled / disabled correctly.
                reloadScreenCaptureDisableUi();
                return true;
            case KEY_MAX_TIME_SCREEN_LOCK:
                try {
                    mDevicePolicyManager.setMaximumTimeToLock(mAdminComponentName,
                            Long.parseLong((String) newValue) * MS_PER_SECOND);
                } catch (NumberFormatException e) {
                    showToast(R.string.not_valid_input);
                }
                return true;
            case KEY_MAX_FAILS_BEFORE_WIPE:
                try {
                    mDevicePolicyManager.setMaximumFailedPasswordsForWipe(mAdminComponentName,
                            Integer.parseInt((String) newValue));
                } catch (NumberFormatException e) {
                    showToast(R.string.not_valid_input);
                }
                return true;
            case STAY_ON_WHILE_PLUGGED_IN:
                mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                        Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                        newValue.equals(true) ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
                updateStayOnWhilePluggedInPreference();
                return true;
            case WIFI_CONFIG_LOCKDOWN_ENABLE_KEY:
                mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                        Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN,
                        newValue.equals(Boolean.TRUE) ?
                                WIFI_CONFIG_LOCKDOWN_ON : WIFI_CONFIG_LOCKDOWN_OFF);
                return true;
            case INSTALL_NONMARKET_APPS_KEY:
                mDevicePolicyManager.setSecureSetting(mAdminComponentName,
                        Settings.Secure.INSTALL_NON_MARKET_APPS,
                        newValue.equals(true) ? "1" : "0");
                updateInstallNonMarketAppsPreference();
                return true;
        }
        return false;
    }

    /**
     * Dispatches an intent to capture image or video.
     */
    private void dispatchCaptureIntent(String action, int requestCode, Uri storageUri) {
        final Intent captureIntent = new Intent(action);
        if (captureIntent.resolveActivity(mPackageManager) != null) {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, storageUri);
            startActivityForResult(captureIntent, requestCode);
        } else {
            showToast(R.string.camera_app_not_found);
        }
    }

    /**
     * Creates a content uri to be used with the capture intent.
     */
    private Uri getStorageUri(String fileName) {
        final String filePath = getActivity().getFilesDir() + File.separator + "media"
                + File.separator + fileName;
        final File file = new File(filePath);
        // Create the folder if it doesn't exist.
        file.getParentFile().mkdirs();
        return FileProvider.getUriForFile(getActivity(),
                "com.afwsamples.testdpc.fileprovider", file);
    }

    /**
     * Shows a list of primary user apps in a dialog.
     *
     * @param dialogTitle the title to show for the dialog
     * @param callback will be called with the list apps that the user has selected when he closes
     *        the dialog. The callback is not fired if the user cancels.
     */
    private void showManageLockTaskListPrompt(int dialogTitle,
            final ManageLockTaskListCallback callback) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> primaryUserAppList = mPackageManager
                .queryIntentActivities(launcherIntent, 0);
        if (primaryUserAppList.isEmpty()) {
            showToast(R.string.no_primary_app_available);
        } else {
            Collections.sort(primaryUserAppList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));
            final LockTaskAppInfoArrayAdapter appInfoArrayAdapter = new LockTaskAppInfoArrayAdapter(
                    getActivity(), R.id.pkg_name, primaryUserAppList);
            ListView listView = new ListView(getActivity());
            listView.setAdapter(appInfoArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    appInfoArrayAdapter.onItemClick(parent, view, position, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(dialogTitle))
                    .setView(listView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] lockTaskEnabledArray = appInfoArrayAdapter.getLockTaskList();
                            callback.onPositiveButtonClicked(lockTaskEnabledArray);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                    .show();
        }
    }

    /**
     * Shows a prompt to collect a package name and checks whether the lock task for the
     * corresponding app is permitted or not.
     */
    private void showCheckLockTaskPermittedPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = getActivity().getLayoutInflater().inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(getString(R.string.input_package_name_hints));

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.check_lock_task_permitted))
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String packageName = input.getText().toString();
                        boolean isLockTaskPermitted = mDevicePolicyManager
                                .isLockTaskPermitted(packageName);
                        showToast(isLockTaskPermitted
                                ? R.string.check_lock_task_permitted_result_permitted
                                : R.string.check_lock_task_permitted_result_not_permitted);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows a prompt to ask for confirmation on wiping the data and also provide an option
     * to set if external storage and factory reset protection data also needs to wiped.
     */
    private void showWipeDataPrompt() {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.wipe_data_dialog_prompt, null);
        final CheckBox externalStorageCheckBox = (CheckBox) dialogView.findViewById(
                R.id.external_storage_checkbox);
        final CheckBox resetProtectionCheckBox = (CheckBox) dialogView.findViewById(
                R.id.reset_protection_checkbox);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.wipe_data_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int flags = 0;
                                flags |= (externalStorageCheckBox.isChecked() ?
                                        DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0);
                                flags |= (resetProtectionCheckBox.isChecked() ?
                                        DevicePolicyManager.WIPE_RESET_PROTECTION_DATA : 0);
                                mDevicePolicyManager.wipeData(flags);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Shows a prompt to ask for confirmation on removing device owner.
     */
    private void showRemoveDeviceOwnerPrompt() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_device_owner_title)
                .setMessage(R.string.remove_device_owner_confirmation)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mDevicePolicyManager.clearDeviceOwnerApp(mPackageName);
                                if (getActivity() != null && !getActivity().isFinishing()) {
                                    showToast(R.string.device_owner_removed);
                                    getActivity().finish();
                                }
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setPreferenceChangeListeners(String[] preferenceKeys) {
        for (String key : preferenceKeys) {
            findPreference(key).setOnPreferenceChangeListener(this);
        }
    }

    /**
     * Updates all preferences in keyguard features section.
     */
    private void updateKeyguardFeaturesUi() {
        int flags = mDevicePolicyManager.getKeyguardDisabledFeatures(mAdminComponentName);
        for (int i = 0; i < KEYGUARD_DISABLE_PREFERENCES.length; i++) {
            int flag = KEYGUARD_DISABLE_FLAGS[i];
            String key = KEYGUARD_DISABLE_PREFERENCES[i];
            SwitchPreference preference = (SwitchPreference) findPreference(key);
            preference.setChecked((flags & flag) != 0);
        }
    }

    private void updateKeyguardDisabledFeatures() {
        int flags = createKeyguardDisabledFlag();
        mDevicePolicyManager.setKeyguardDisabledFeatures(mAdminComponentName, flags);
        updateKeyguardFeaturesUi();
    }

    /**
     * Update the preference switch for {@link Settings.Global#STAY_ON_WHILE_PLUGGED_IN} setting.
     *
     * <p>
     * If either one of the {@link BatteryManager#BATTERY_PLUGGED_AC},
     * {@link BatteryManager#BATTERY_PLUGGED_USB}, {@link BatteryManager#BATTERY_PLUGGED_WIRELESS}
     * values is set, we toggle the preference to true and update the setting value to
     * {@link #BATTERY_PLUGGED_ANY}
     * </p>
     */
    private void updateStayOnWhilePluggedInPreference() {
        if (!mStayOnWhilePluggedInSwitchPreference.isEnabled()) {
            return;
        }

        boolean checked = false;
        final int currentState = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);
        checked = (currentState &
                (BatteryManager.BATTERY_PLUGGED_AC |
                BatteryManager.BATTERY_PLUGGED_USB |
                BatteryManager.BATTERY_PLUGGED_WIRELESS)) != 0;
        mDevicePolicyManager.setGlobalSetting(mAdminComponentName,
                Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                checked ? BATTERY_PLUGGED_ANY : DONT_STAY_ON);
        mStayOnWhilePluggedInSwitchPreference.setChecked(checked);
    }

    /**
     * Update the preference switch for {@link Settings.Secure#INSTALL_NON_MARKET_APPS} setting.
     *
     * <p>
     * If the user restriction {@link UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES} is set, then
     * we disable this preference.
     * </p>
     */
    public void updateInstallNonMarketAppsPreference() {
        mInstallNonMarketAppsPreference.setEnabled(
                mUserManager.hasUserRestriction(DISALLOW_INSTALL_UNKNOWN_SOURCES) ? false : true);
        int isInstallNonMarketAppsAllowed = Settings.Secure.getInt(
                getActivity().getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 0);
        mInstallNonMarketAppsPreference.setChecked(
                isInstallNonMarketAppsAllowed == 0 ? false : true);
    }

    /**
     * Creates the keyguard disabled flag for sending back to DevicePolicyManager.
     */
    private int createKeyguardDisabledFlag() {
        int flags = DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE;
        for (int i = 0; i < KEYGUARD_DISABLE_PREFERENCES.length; i++) {
            int flag = KEYGUARD_DISABLE_FLAGS[i];
            String key = KEYGUARD_DISABLE_PREFERENCES[i];
            SwitchPreference preference = (SwitchPreference) findPreference(key);
            flags |= preference.isChecked() ? flag : 0;
        }
        return flags;
    }

    /**
     * Some functionality only works if this app is device owner. Disable their UIs to avoid
     * confusion.
     */
    private void disableIncompatibleManagementOptionsInCurrentProfile() {
        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(mPackageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(mPackageName);
        int deviceOwnerStatusStringId = R.string.this_is_not_a_device_owner;
        if (isProfileOwner) {
            // Some of the management options can only be applied in a primary profile.
            for (String preference : PRIMARY_USER_ONLY_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
            if (isBeforeM()) {
                for (String preference : MANAGED_PROFILE_MNC_PLUS_PREFERENCES) {
                    findPreference(preference).setEnabled(false);
                }
            }
            deviceOwnerStatusStringId = R.string.this_is_a_managed_profile_owner;
        } else if (isDeviceOwner) {
            // If it's a device owner and running in the primary profile.
            deviceOwnerStatusStringId = R.string.this_is_a_device_owner;
            for (String managedProfileSpecificOption : MANAGED_PROFILE_SPECIFIC_OPTIONS) {
                findPreference(managedProfileSpecificOption).setEnabled(false);
            }
        }
        findPreference(DEVICE_OWNER_STATUS_KEY).setSummary(deviceOwnerStatusStringId);
        if (!isDeviceOwner) {
            findPreference(WIFI_CONFIG_LOCKDOWN_ENABLE_KEY).setEnabled(false);
        }
    }

    private void disableIncompatibleManagementOptionsByApiLevel() {
        if (isBeforeM()) {
            // The following options depend on MNC APIs.
            for (String preference : MNC_PLUS_PREFERENCES) {
                findPreference(preference).setEnabled(false);
            }
        }
    }

    private boolean isBeforeM() {
        return Build.VERSION.SDK_INT < VERSION_CODES.M;
    }

    /**
     * Shows the default response for future runtime permission requests by applications, and lets
     * the user change the default value.
     */
    private void showSetPermissionPolicyDialog() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View setPermissionPolicyView = getActivity().getLayoutInflater().inflate(
                R.layout.set_permission_policy, null);
        final RadioGroup permissionGroup =
                (RadioGroup) setPermissionPolicyView.findViewById(R.id.set_permission_group);

        int permissionPolicy = mDevicePolicyManager.getPermissionPolicy(mAdminComponentName);
        switch (permissionPolicy) {
            case DevicePolicyManager.PERMISSION_POLICY_PROMPT:
                ((RadioButton) permissionGroup.findViewById(R.id.prompt)).toggle();
                break;
            case DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT:
                ((RadioButton) permissionGroup.findViewById(R.id.accept)).toggle();
                break;
            case DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY:
                ((RadioButton) permissionGroup.findViewById(R.id.deny)).toggle();
                break;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.set_default_permission_policy))
                .setView(setPermissionPolicyView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int policy = 0;
                        int checked = permissionGroup.getCheckedRadioButtonId();
                        switch (checked) {
                            case (R.id.prompt):
                                policy = DevicePolicyManager.PERMISSION_POLICY_PROMPT;
                                break;
                            case (R.id.accept):
                                policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT;
                                break;
                            case (R.id.deny):
                                policy = DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY;
                                break;
                        }
                        mDevicePolicyManager.setPermissionPolicy(mAdminComponentName, policy);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Shows a prompt that allows entering the account type for which account management should be
     * disabled or enabled.
     */
    private void showSetDisableAccountManagementPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.account_type_hint);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.set_disable_account_management)
                .setView(view)
                .setPositiveButton(R.string.disable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String accountType = input.getText().toString();
                        setDisableAccountManagement(accountType, true);
                    }
                })
                .setNeutralButton(R.string.enable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String accountType = input.getText().toString();
                        setDisableAccountManagement(accountType, false);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null /* Nothing to do */)
                .show();
    }

    private void setDisableAccountManagement(String accountType, boolean disabled) {
        if (!TextUtils.isEmpty(accountType)) {
            mDevicePolicyManager.setAccountManagementDisabled(mAdminComponentName, accountType,
                    disabled);
            showToast(disabled
                            ? R.string.account_management_disabled
                            : R.string.account_management_enabled,
                    accountType);
            return;
        }
        showToast(R.string.fail_to_set_account_management);
    }

    /**
     * Shows a list of account types that is disabled for account management.
     */
    private void showDisableAccountTypeList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        String[] disabledAccountTypeList = mDevicePolicyManager
                .getAccountTypesWithManagementDisabled();
        Arrays.sort(disabledAccountTypeList, String.CASE_INSENSITIVE_ORDER);
        if (disabledAccountTypeList == null || disabledAccountTypeList.length == 0) {
            showToast(R.string.no_disabled_account);
        } else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.list_of_disabled_account_types)
                    .setAdapter(new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1, android.R.id.text1,
                            disabledAccountTypeList), null)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    /**
     * For user creation:
     * Shows a prompt to ask for the username that would be used for creating a new user.
     */
    private void showCreateUserPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.enter_username_hint);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_and_initialize_user)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = input.getText().toString();
                        String ownerName = getString(R.string.app_name);
                        if (!TextUtils.isEmpty(name)) {
                            UserHandle userHandle = mDevicePolicyManager.createAndInitializeUser(
                                    mAdminComponentName, name, ownerName, mAdminComponentName,
                                    new Bundle());
                            if (userHandle != null) {
                                long serialNumber = mUserManager.getSerialNumberForUser(userHandle);
                                showToast(R.string.user_created, serialNumber);
                                return;
                            }
                            showToast(R.string.failed_to_create_user);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * For user removal:
     * Shows a prompt for a user serial number. The associated user will be removed.
     */
    private void showRemoveUserPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(R.string.enter_user_id);
        input.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_user)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean success = false;
                        long serialNumber = -1;
                        try {
                            serialNumber = Long.parseLong(input.getText().toString());
                            UserHandle userHandle = mUserManager
                                    .getUserForSerialNumber(serialNumber);
                            if (userHandle != null) {
                                success = mDevicePolicyManager
                                        .removeUser(mAdminComponentName, userHandle);
                            }
                        } catch (NumberFormatException e) {
                            // Error message is printed in the next line.
                        }
                        showToast(success ? R.string.user_removed : R.string.failed_to_remove_user);
                    }
                })
                .show();
    }

    /**
     * Asks for the package name whose uninstallation should be blocked / unblocked.
     */
    private void showBlockUninstallationByPackageNamePrompt() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.simple_edittext, null);
        final EditText input = (EditText) view.findViewById(R.id.input);
        input.setHint(getString(R.string.input_package_name_hints));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.block_uninstallation_title)
                .setView(view)
                .setPositiveButton(R.string.block, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pkgName = input.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)) {
                            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName,
                                    true);
                            showToast(R.string.uninstallation_blocked, pkgName);
                        } else {
                            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
                        }
                    }
                })
                .setNeutralButton(R.string.unblock, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String pkgName = input.getText().toString();
                        if (!TextUtils.isEmpty(pkgName)) {
                            mDevicePolicyManager.setUninstallBlocked(mAdminComponentName, pkgName,
                                    false);
                            showToast(R.string.uninstallation_allowed, pkgName);
                        } else {
                            showToast(R.string.block_uninstallation_failed_invalid_pkgname);
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void reloadCameraDisableUi() {
        boolean isCameraDisabled = mDevicePolicyManager.getCameraDisabled(mAdminComponentName);
        mDisableCameraSwitchPreference.setChecked(isCameraDisabled);
    }

    private void reloadScreenCaptureDisableUi() {
        boolean isScreenCaptureDisabled = mDevicePolicyManager.getScreenCaptureDisabled(
                mAdminComponentName);
        mDisableScreenCaptureSwitchPreference.setChecked(isScreenCaptureDisabled);
    }

    /**
     * Shows a prompt to ask for package name which is used to enable a system app.
     */
    private void showEnableSystemAppByPackageNamePrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        LinearLayout inputContainer = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.simple_edittext, null);
        final EditText editText = (EditText) inputContainer.findViewById(R.id.input);
        editText.setHint(getString(R.string.enable_system_apps_by_package_name_hints));

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.enable_system_apps_title))
                .setView(inputContainer)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String packageName = editText.getText().toString();
                        try {
                            mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                            showToast(R.string.enable_system_apps_by_package_name_success_msg,
                                    packageName);
                        } catch (IllegalArgumentException e) {
                            showToast(R.string.enable_system_apps_by_package_name_error);
                        } finally {
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showInstallAppPrompt() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        LinearLayout inputContainer = (LinearLayout) getActivity().getLayoutInflater()
                .inflate(R.layout.install_update_app, null);

        final EditText packageName = (EditText) inputContainer.findViewById(R.id.package_name);
        final EditText apkPath = (EditText) inputContainer.findViewById(R.id.apk_path);

        new AlertDialog.Builder(getActivity())
                .setTitle("Install or update an app")
                .setView(inputContainer)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String packageNameValue = packageName.getText().toString();
                        final String apkPathValue = apkPath.getText().toString();

                        showToast("Package: " + packageNameValue + ", apk: " + apkPathValue);
                    }
                });
    }

    /**
     * Shows the file viewer for importing a certificate.
     */
    private void showFileViewerForImportingCertificate(int requestCode) {
        Intent certIntent = new Intent(Intent.ACTION_GET_CONTENT);
        certIntent.setTypeAndNormalize("*/*");
        try {
            startActivityForResult(certIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Imports a certificate to the managed profile. If the provided password failed to decrypt the
     * given certificate, shows a try again prompt. Otherwise, shows a prompt for the certificate
     * alias.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param password The password to decrypt the certificate.
     */
    private void importKeyCertificateFromIntent(Intent intent, String password) {
        importKeyCertificateFromIntent(intent, password, 0 /* first try */);
    }

    /**
     * Imports a certificate to the managed profile. If the provided decryption password is
     * incorrect, shows a try again prompt. Otherwise, shows a prompt for the certificate alias.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param password The password to decrypt the certificate.
     * @param attempts The number of times user entered incorrect password.
     */
    private void importKeyCertificateFromIntent(Intent intent, String password, int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Uri data = null;
        if (intent != null && (data = intent.getData()) != null) {
            // If the password is null, try to decrypt the certificate with an empty password.
            if (password == null) {
                password = "";
            }
            InputStream certificateInputStream;
            try {
                certificateInputStream = getActivity().getContentResolver().openInputStream(data);
                KeyStore keyStore = KeyStore.getInstance(KeyChain.EXTRA_PKCS12);
                keyStore.load(certificateInputStream, password.toCharArray());
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    if (!TextUtils.isEmpty(alias)) {
                        Certificate certificate = keyStore.getCertificate(alias);
                        PrivateKey privateKey = (PrivateKey) keyStore
                                .getKey(alias, "".toCharArray());
                        showPromptForKeyCertificateAlias(privateKey, certificate, alias);
                    }
                }
            } catch (KeyStoreException | FileNotFoundException | CertificateException
                    | UnrecoverableKeyException | NoSuchAlgorithmException e) {
                Log.e(TAG, "Unable to load key", e);
            } catch (IOException e) {
                showPromptForCertificatePassword(intent, ++attempts);
            } catch (ClassCastException e) {
                showToast(R.string.not_a_key_certificate);
            }
        }
    }

    /**
     * Shows a prompt to ask for the certificate password. If the certificate password is correct,
     * import the private key and certificate.
     *
     * @param intent Intent that contains the certificate data uri.
     * @param attempts The number of times user entered incorrect password.
     */
    private void showPromptForCertificatePassword(final Intent intent, final int attempts) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        View passwordInputView = getActivity().getLayoutInflater()
                .inflate(R.layout.certificate_password_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.password_input);
        if (attempts > 1) {
            passwordInputView.findViewById(R.id.incorrect_password).setVisibility(View.VISIBLE);
        }
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.certificate_password_prompt_title))
                .setView(passwordInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userPassword = input.getText().toString();
                        importKeyCertificateFromIntent(intent, userPassword, attempts);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Shows a prompt to ask for the certificate alias. This alias will be imported together with
     * the private key and certificate.
     *
     * @param key The private key of a certificate.
     * @param certificate The certificate will be imported.
     * @param alias A name that represents the certificate in the profile.
     */
    private void showPromptForKeyCertificateAlias(final PrivateKey key,
            final Certificate certificate, String alias) {
        if (getActivity() == null || getActivity().isFinishing() || key == null
                || certificate == null) {
            return;
        }
        View passwordInputView = getActivity().getLayoutInflater().inflate(
                R.layout.certificate_alias_prompt, null);
        final EditText input = (EditText) passwordInputView.findViewById(R.id.alias_input);
        if (!TextUtils.isEmpty(alias)) {
            input.setText(alias);
            input.selectAll();
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.certificate_alias_prompt_title))
                .setView(passwordInputView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String alias = input.getText().toString();
                        if (mDevicePolicyManager.installKeyPair(mAdminComponentName, key,
                                certificate, alias) == true) {
                            showToast(R.string.certificate_added, alias);
                        } else {
                            showToast(R.string.certificate_add_failed, alias);
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Imports a CA certificate from the given data URI.
     *
     * @param intent Intent that contains the CA data URI.
     */
    private void importCaCertificateFromIntent(Intent intent) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        Uri data = null;
        if (intent != null && (data = intent.getData()) != null) {
            boolean isCaInstalled = false;
            try {
                InputStream certificateInputStream = getActivity().getContentResolver()
                        .openInputStream(data);
                if (certificateInputStream != null) {
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int len = 0;
                    while ((len = certificateInputStream.read(buffer)) > 0) {
                        byteBuffer.write(buffer, 0, len);
                    }
                    isCaInstalled = mDevicePolicyManager.installCaCert(mAdminComponentName,
                            byteBuffer.toByteArray());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            showToast(isCaInstalled ? R.string.install_ca_successfully : R.string.install_ca_fail);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case INSTALL_KEY_CERTIFICATE_REQUEST_CODE:
                    importKeyCertificateFromIntent(data, "");
                    break;
                case INSTALL_CA_CERTIFICATE_REQUEST_CODE:
                    importCaCertificateFromIntent(data);
                    break;
                case CAPTURE_IMAGE_REQUEST_CODE:
                    showFragment(MediaDisplayFragment.newInstance(
                            MediaDisplayFragment.REQUEST_DISPLAY_IMAGE, mImageUri));
                    break;
                case CAPTURE_VIDEO_REQUEST_CODE:
                    showFragment(MediaDisplayFragment.newInstance(
                            MediaDisplayFragment.REQUEST_DISPLAY_VIDEO, mVideoUri));
                    break;
            }
        }
    }

    /**
     * Shows a list of installed CA certificates.
     */
    private void showCaCertificateList() {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        // Avoid starting the same task twice.
        if (mShowCaCertificateListTask != null && !mShowCaCertificateListTask.isCancelled()) {
            mShowCaCertificateListTask.cancel(true);
        }
        mShowCaCertificateListTask = new ShowCaCertificateListTask();
        mShowCaCertificateListTask.execute();
    }

    /**
     * Displays an alert dialog that allows the user to select applications from all non-system
     * applications installed on the current profile. After the user selects an app, this app can't
     * be uninstallation.
     */
    private void showBlockUninstallationPrompt() {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        List<ApplicationInfo> applicationInfoList
                = mPackageManager.getInstalledApplications(0 /* No flag */);
        List<ResolveInfo> resolveInfoList = new ArrayList<ResolveInfo>();
        Collections.sort(applicationInfoList,
                new ApplicationInfo.DisplayNameComparator(mPackageManager));
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            // Ignore system apps because they can't be uninstalled.
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                ResolveInfo resolveInfo = new ResolveInfo();
                resolveInfo.resolvePackageName = applicationInfo.packageName;
                resolveInfoList.add(resolveInfo);
            }
        }

        final BlockUninstallationInfoArrayAdapter blockUninstallationInfoArrayAdapter
                = new BlockUninstallationInfoArrayAdapter(getActivity(), R.id.pkg_name,
                resolveInfoList);
        ListView listview = new ListView(getActivity());
        listview.setAdapter(blockUninstallationInfoArrayAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                blockUninstallationInfoArrayAdapter.onItemClick(parent, view, pos, id);
            }
        });

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.block_uninstallation_title)
                .setView(listview)
                .setPositiveButton(R.string.close, null /* Nothing to do */)
                .show();
    }

    /**
     * Shows an alert dialog which displays a list of disabled system apps. Clicking an app in the
     * dialog enables the app.
     */
    private void showEnableSystemAppsPrompt() {
        // Disabled system apps list = {All system apps} - {Enabled system apps}
        final List<String> disabledSystemApps = new ArrayList<String>();
        // This list contains both enabled and disabled apps.
        List<ApplicationInfo> allApps = mPackageManager.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
        // This list contains all enabled apps.
        List<ApplicationInfo> enabledApps =
                mPackageManager.getInstalledApplications(0 /* Default flags */);
        Set<String> enabledAppsPkgNames = new HashSet<String>();
        for (ApplicationInfo applicationInfo : enabledApps) {
            enabledAppsPkgNames.add(applicationInfo.packageName);
        }
        for (ApplicationInfo applicationInfo : allApps) {
            // Interested in disabled system apps only.
            if (!enabledAppsPkgNames.contains(applicationInfo.packageName)
                    && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                disabledSystemApps.add(applicationInfo.packageName);
            }
        }

        if (disabledSystemApps.isEmpty()) {
            showToast(R.string.no_disabled_system_apps);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.id.pkg_name, disabledSystemApps, true);
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.enable_system_apps_title))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = disabledSystemApps.get(position);
                            mDevicePolicyManager.enableSystemApp(mAdminComponentName, packageName);
                            showToast(R.string.enable_system_apps_by_package_name_success_msg,
                                    packageName);
                        }
                    })
                    .show();
        }
    }

    /**
     * Shows an alert dialog which displays a list hidden / non-hidden apps. Clicking an app in the
     * dialog enables the app.
     */
    private void showHideAppsPrompt(final boolean showHiddenApps) {
        final List<String> showApps = new ArrayList<> ();
        if (showHiddenApps) {
            // Find all hidden packages using the GET_UNINSTALLED_PACKAGES flag
            List<ApplicationInfo> allApps = mPackageManager.getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            Collections.sort(allApps, new ApplicationInfo.DisplayNameComparator(mPackageManager));
            for (ApplicationInfo applicationInfo : allApps) {
                if (mDevicePolicyManager.isApplicationHidden(mAdminComponentName,
                        applicationInfo.packageName)) {
                    showApps.add(applicationInfo.packageName);
                }
            }
        } else {
            // Find all non-hidden apps with a launcher icon
            final Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> launcherIntentResolvers = mPackageManager
                    .queryIntentActivities(launcherIntent, 0);
            Collections.sort(launcherIntentResolvers,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));
            for (ResolveInfo res : launcherIntentResolvers) {
                if (!showApps.contains(res.activityInfo.packageName)) {
                    showApps.add(res.activityInfo.packageName);
                }
            }
        }

        if (showApps.isEmpty()) {
            showToast(showHiddenApps ? R.string.unhide_apps_empty : R.string.hide_apps_empty);
        } else {
            AppInfoArrayAdapter appInfoArrayAdapter = new AppInfoArrayAdapter(getActivity(),
                    R.id.pkg_name, showApps, true);
            final int dialogTitleResId;
            final int successResId;
            final int failureResId;
            if (showHiddenApps) {
                // showing a dialog to unhide an app
                dialogTitleResId = R.string.unhide_apps_title;
                successResId = R.string.unhide_apps_success;
                failureResId = R.string.unhide_apps_failure;
            } else {
                // showing a dialog to hide an app
                dialogTitleResId = R.string.hide_apps_title;
                successResId = R.string.hide_apps_success;
                failureResId = R.string.hide_apps_failure;
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(dialogTitleResId))
                    .setAdapter(appInfoArrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            String packageName = showApps.get(position);
                            if (mDevicePolicyManager.setApplicationHidden(mAdminComponentName,
                                    packageName, !showHiddenApps)) {
                                showToast(successResId, packageName);
                            } else {
                                showToast(getString(failureResId, packageName), Toast.LENGTH_LONG);
                            }
                        }
                    })
                    .show();
        }
    }

    private void showToast(int msgId, Object... args) {
        showToast(getString(msgId, args));
    }

    private void showToast(String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    private void showToast(String msg, int duration) {
        Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Toast.makeText(activity, msg, duration).show();
    }

    /**
     * Gets all the accessibility services. After all the accessibility services are retrieved, the
     * result is displayed in a popup.
     */
    private class GetAccessibilityServicesTask extends
            AsyncTask<Void, Void, List<AccessibilityServiceInfo>> {

        private AccessibilityManager mAccessibilityManager;

        public GetAccessibilityServicesTask() {
            mAccessibilityManager = (AccessibilityManager) getActivity().getSystemService(
                    Context.ACCESSIBILITY_SERVICE);
        }

        @Override
        protected List<AccessibilityServiceInfo> doInBackground(Void... voids) {
            return mAccessibilityManager.getInstalledAccessibilityServiceList();
        }

        @Override
        protected void onPostExecute(List<AccessibilityServiceInfo> accessibilityServicesInfoList) {
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            List<ResolveInfo> accessibilityServicesResolveInfoList
                    = AccessibilityServiceInfoArrayAdapter
                    .getResolveInfoListFromAccessibilityServiceInfoList(
                            accessibilityServicesInfoList);
            Collections.sort(accessibilityServicesResolveInfoList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));
            final AccessibilityServiceInfoArrayAdapter accessibilityServiceInfoArrayAdapter
                    = new AccessibilityServiceInfoArrayAdapter(getActivity(), R.id.pkg_name,
                            accessibilityServicesResolveInfoList);
            ListView listview = new ListView(getActivity());
            listview.setAdapter(accessibilityServiceInfoArrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    accessibilityServiceInfoArrayAdapter.onItemClick(parent, view, pos, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.set_accessibility_services)
                    .setView(listview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<String> permittedAccessibilityServicesArrayList
                                    = accessibilityServiceInfoArrayAdapter
                                    .getSelectedAccessibilityServices();
                            boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(
                                    DeviceAdminReceiver.getComponentName(getActivity()),
                                    permittedAccessibilityServicesArrayList);
                            showToast(result
                                    ? R.string.set_accessibility_services_successful
                                    : R.string.set_accessibility_services_fail);
                        }
                    })
                    .setNeutralButton(R.string.allow_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean result = mDevicePolicyManager.setPermittedAccessibilityServices(
                                    mAdminComponentName, null);
                            showToast(result
                                    ? R.string.all_accessibility_services_enabled
                                    : R.string.set_accessibility_services_fail);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    /**
     * Gets all the input methods and displays them in a prompt.
     */
    private class GetInputMethodsTask extends AsyncTask<Void, Void, List<InputMethodInfo>> {

        private InputMethodManager mInputMethodManager;

        public GetInputMethodsTask() {
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
        }

        @Override
        protected List<InputMethodInfo> doInBackground(Void... voids) {
            return mInputMethodManager.getInputMethodList();
        }

        @Override
        protected void onPostExecute(List<InputMethodInfo> inputMethodsInfoList) {
            Activity activity = getActivity();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            List<ResolveInfo> inputMethodsResolveInfoList
                    = InputMethodInfoArrayAdapter.getResolveInfoListFromInputMethodsInfoList(
                    inputMethodsInfoList);
            Collections.sort(inputMethodsResolveInfoList,
                    new ResolveInfo.DisplayNameComparator(mPackageManager));

            final InputMethodInfoArrayAdapter inputMethodInfoArrayAdapter
                    = new InputMethodInfoArrayAdapter(getActivity(), R.id.pkg_name,
                            inputMethodsResolveInfoList);
            ListView listview = new ListView(getActivity());
            listview.setAdapter(inputMethodInfoArrayAdapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    inputMethodInfoArrayAdapter.onItemClick(parent, view, pos, id);
                }
            });

            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.set_input_methods)
                    .setView(listview)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ArrayList<String> permittedAccessibilityServicesArrayList
                                    = inputMethodInfoArrayAdapter.getSelectedInputMethods();
                            boolean result = mDevicePolicyManager.setPermittedInputMethods(
                                    DeviceAdminReceiver.getComponentName(getActivity()),
                                    permittedAccessibilityServicesArrayList);
                            showToast(result
                                    ? R.string.set_input_methods_successful
                                    : R.string.set_input_methods_fail);
                        }
                    })
                    .setNeutralButton(R.string.allow_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean result = mDevicePolicyManager.setPermittedInputMethods(
                                    mAdminComponentName, null);
                            showToast(result
                                    ? R.string.all_input_methods_enabled
                                    : R.string.set_input_methods_fail);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    /**
     * Gets all CA certificates and displays them in a prompt.
     */
    private class ShowCaCertificateListTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            return getCaCertificateSubjectDnList();
        }

        @Override
        protected void onPostExecute(String[] installedCaCertificateDnList) {
            if (getActivity() == null || getActivity().isFinishing()) {
                return;
            }
            if (installedCaCertificateDnList == null) {
                showToast(R.string.no_ca_certificate);
            } else {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.installed_ca_title))
                        .setItems(installedCaCertificateDnList, null)
                        .show();
            }
        }

        private String[] getCaCertificateSubjectDnList() {
            List<byte[]> installedCaCerts = mDevicePolicyManager.getInstalledCaCerts(
                    mAdminComponentName);
            String[] caSubjectDnList = null;
            if (installedCaCerts.size() > 0) {
                caSubjectDnList = new String[installedCaCerts.size()];
                int i = 0;
                for (byte[] installedCaCert : installedCaCerts) {
                    try {
                        X509Certificate certificate = (X509Certificate) CertificateFactory
                                .getInstance(X509_CERT_TYPE).generateCertificate(
                                        new ByteArrayInputStream(installedCaCert));
                        caSubjectDnList[i++] = certificate.getSubjectDN().getName();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    }
                }
            }
            return caSubjectDnList;
        }
    }

    private void showFragment(final Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().addToBackStack(PolicyManagementFragment.class.getName())
                .replace(R.id.container, fragment).commit();
    }

    private void startKioskMode(String[] lockTaskArray) {
        // start locked activity
        Intent launchIntent = new Intent(getActivity(), KioskModeActivity.class);
        launchIntent.putExtra(KioskModeActivity.LOCKED_APP_PACKAGE_LIST, lockTaskArray);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mPackageManager.setComponentEnabledSetting(
                new ComponentName(mPackageName, KioskModeActivity.class.getName()),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        startActivity(launchIntent);
        getActivity().finish();
    }

    private void showWifiConfigCreationDialog() {
        WifiConfigCreationDialog dialog = WifiConfigCreationDialog.newInstance();
        dialog.show(getFragmentManager(), TAG_WIFI_CONFIG_CREATION);
    }

     abstract class ManageLockTaskListCallback {
        public abstract void onPositiveButtonClicked(String[] lockTaskArray);
    }
}
