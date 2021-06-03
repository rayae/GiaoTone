package cn.bavelee.giaotone;

public class Consts {

    public static final String APP_PACKAGE_NAME = "cn.bavelee.giaotone";

    public static final int[] INTERNAL_RAW_SOUNDS = new int[]{R.raw.mtyjgmwz_hmbb, R.raw.mtyjgmwz_girl, R.raw.giao, R.raw.memeda, R.raw.timo};
    public static final String KEY_AUDIO_USAGE = "key_audio_usage";
    public static final String KEY_SOUND_STANDALONE_VOLUME = "settings_sound_standalone_volume";
    public static final String DEFAULT_AUDIO_USAGE = "default";
    public static final int DEFAULT_SOUND_ID = 1;
    public static final String AUDIO_SUB_DIR = "/audio";

    public static final String KEY_IS_SHOWED_TIPS = "key_is_showed_tips";
    public static final String KEY_AUTO_START = "key_auto_start";
    public static final String KEY_EXCLUDE_FROM_RECENTS = "key_exclude_from_recents";
    public static final String KEY_CUSTOM_TIME_ENABLED = "key_custom_time_enabled";
    public static final String KEY_USE_FLOATING_SERVICE = "key_use_floating_service";

    public static final String KEY_AVOID_MISTAKE_TOUCH = "key_avoid_mistake_touch";
    public static final String DEFAULT_AVOID_TIME_NONE = "none";

    public static final String KEY_AVOID_ACCIDENTAL_PLUG_OUT = "key_avoid_accidental_plug_out";

    public static final String KEY_SOUND_PICKER = "key_sound_picker";
    public static final String SOUND_PICKER_FILE_MANAGER = "file_manager";
    public static final String SOUND_PICKER_INTERNAL_SEARCHER = "internal_audio_searcher";
    public static final String DEFAULT_SOUND_PICKER = SOUND_PICKER_FILE_MANAGER;


    public static final String KEY_CUSTOM_TIME_START = "key_custom_time_start";
    public static final String KEY_CUSTOM_TIME_END = "key_custom_time_end";
    public static final String DEFAULT_AVAILABLE_TIME = "00:00";
    public static final boolean DEFAULT_CUSTOM_TIME_ENABLED = false;

    public static final String KEY_MUTE_WHILE_PLAYING = "key_mute_while_playing";
    public static final boolean DEFAULT_MUTE_WHILE_PLAYING = false;


    public static final String KEY_IGNORE_BATTERY_OPTIMIZATION = "key_ignore_battery_optimization";


    public static final String KEY_SHOW_LOG = "key_show_log";
    public static final String KEY_CHECK_SERVICE = "key_check_service";
    public static final String KEY_RAPID_CHECK_SCREEN_OFF = "key_rapid_check_screen_off";


    public static final String KEY_MAIN_SERVER = "key_main_server";
    public static String DEFAULT_MAIN_SERVER;
    public static String SERVER;
    public static String URL_SOUND_LIBRARY;

    public static final int REQUEST_CODE_PICK_EXTERNAL_SOUND = 101;
    public static final int REQUEST_CODE_WRITE_PERMISSION = 102;
    public static final int REQUEST_CODE_FLOATING_SERVICE_PERMISSION = 103;


    public static final int BATTERY_LEVEL_COUNT = 5;

}
