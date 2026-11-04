// IMPORTANT: REQUIRES UNMERGED https://github.com/libusb/libusb/pull/1164
#define PY_SSIZE_T_CLEAN
#include <string>
#include <jni.h>
#include <unistd.h>
#include "android/log.h"
#include <vector>
#include <stdio.h>
#include <libusb/libusb.h>
#include <iostream>
#include <numeric>
#include <fstream>

#define  LOG_TAG    "pandaflash"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)


struct McuConfig {
  std::string mcu;
  int mcu_idcode;
  std::vector<int> sector_sizes;
  int sector_count;
  int uid_address;
  int block_size;
  int serial_number_address;
  int app_address;
  std::string app_fn;
  int bootstub_address;
  std::string bootstub_fn;

  int sector_address(int i) const {
    // assume bootstub is in sector 0
    int address = bootstub_address;
    for (int j = 0; j < i; ++j) {
      address += sector_sizes[j];
    }
    return address;
  }
};

// Define F4Config and H7Config constants
const McuConfig F4Config = {
  "STM32F4",
  0x463,
  {0x4000, 0x4000, 0x4000, 0x4000, 0x10000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000},
  16,
  0x1FFF7A10,
  0x800,
  0x1FFF79C0,
  0x8004000,
  "panda.bin.signed",
  0x8000000,
  "bootstub.panda.bin"
};

const McuConfig H7Config = {
  "STM32H7",
  0x483,
  {0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000, 0x20000},
  8,
  0x1FF1E800,
  0x400,
  0x080FFFC0,
  0x8020000,
  "panda_h7.bin.signed",
  0x8000000,
  "bootstub.panda_h7.bin"
};

enum class McuType {
  F4,
  H7,
  UNKNOWN
};

McuConfig get_mcu_config(McuType mcu_type) {
  if (mcu_type == McuType::H7) {
    return H7Config;
  }
  return F4Config;
}

void open_panda(libusb_context* ctx, libusb_device_handle** dev_handle) {
  ssize_t num_devices;
  libusb_device** dev_list = NULL;

  while (1) {
    num_devices = libusb_get_device_list(ctx, &dev_list);
    for (size_t i = 0; i < num_devices; ++i) {
      libusb_device_descriptor desc;
      libusb_get_device_descriptor(dev_list[i], &desc);

      // Check for both panda vendor IDs and product IDs
      if ((desc.idVendor == 0xbbaa || desc.idVendor == 0x3801) &&
          (desc.idProduct == 0xddcc || desc.idProduct == 0xddee)) {
        int ret = 0;
        ret = libusb_open(dev_list[i], dev_handle);
        if(ret == 0) {
          return;
        }
      }
    }
    usleep(500000L);
  }
}

McuType get_mcu_type(libusb_device_handle *dev_handle) {
  // Get hardware type
  unsigned char hw_type[0x40];
  int r = libusb_control_transfer(
      dev_handle,
      LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xc1, // bRequest
      0, // wValue
      0, // wIndex
      hw_type, // Data buffer
      0x40, // Length of data buffer
      0 // Timeout in milliseconds
  );
  if (r < 0) {
    return McuType::UNKNOWN;
  }
  if (hw_type[0] == 0x01 || hw_type[0] == 0x02 || hw_type[0] == 0x03) {
    return McuType::F4;
  } else if (hw_type[0] == 0x07 || hw_type[0] == 0x08) {
    return McuType::H7;
  } else {
    return McuType::UNKNOWN;
  }
}

void reset(libusb_device_handle *dev_handle, bool bootstub) {
  libusb_control_transfer(
          dev_handle,
          LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
          0xd1, // bRequest
          bootstub ? 1 : 0, // wValue (1 for bootstub, 0 for normal reset)
          0, // wIndex
          nullptr, // Data buffer (not used here)
          0, // Length of data buffer
          15000 // Timeout in milliseconds
      );
}

bool flasher_present(libusb_device_handle* handle) {
  std::vector<uint8_t> fr(0x0c); // Buffer to store the received data

  int bytes_received = libusb_control_transfer(
      handle,
      LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xb0, // bRequest
      0, // wValue
      0, // wIndex
      fr.data(), // Data buffer
      fr.size(), // Length of data buffer
      0 // Timeout in milliseconds
  );

  if (bytes_received != 0x0c) {
    LOGE("Failed to read data from device");
    return false;
  }

  // Check if bytes 4 to 7 match the expected signature
  return (fr[4] == 0xde && fr[5] == 0xad && fr[6] == 0xd0 && fr[7] == 0x0d);
}

bool flash_static(libusb_device_handle* handle, std::vector<uint8_t>& code, McuConfig mcu_config) {
  // Determine sectors to erase
  std::vector<int> apps_sectors_cumsum(mcu_config.sector_sizes.size() - 1);
  std::partial_sum(mcu_config.sector_sizes.begin() + 1, mcu_config.sector_sizes.end(), apps_sectors_cumsum.begin());

  int last_sector = -1;
  for (size_t i = 0; i < apps_sectors_cumsum.size(); ++i) {
    if (apps_sectors_cumsum[i] > code.size()) {
      last_sector = i + 1;
      break;
    }
  }

  if (last_sector < 1) {
    LOGE("Binary too small? No sector to erase.");
    return false;
  }
  if (last_sector >= 7) {
    LOGE("Binary too large! Risk of overwriting provisioning chunk.");
    return false;
  }

  // Unlock flash
  LOGI("flash: unlocking");
  int r = libusb_control_transfer(
      handle,
      LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xb1, // bRequest
      0, // wValue
      0, // wIndex
      nullptr, // Data buffer (not used here)
      0, // Length of data buffer
      0 // Timeout in milliseconds
  );
  if (r < 0) {
    LOGE("Failed to unlock flash: %d", r);
    return false;
  }

  // Erase sectors
  LOGI("flash: erasing sectors 1 - %d", last_sector);
  for (int i = 1; i <= last_sector; ++i) {
    r = libusb_control_transfer(
        handle,
        LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
        0xb2, // bRequest
        i, // wValue (sector number)
        0, // wIndex
        nullptr, // Data buffer (not used here)
        0, // Length of data buffer
        0 // Timeout in milliseconds
    );
    if (r < 0) {
      LOGE("Failed to erase sector %d: %d", i, r);
      return false;
    }
  }

  // Flash over EP2
  constexpr int STEP = 0x10;
  LOGI("flash: flashing");
  for (size_t i = 0; i < code.size(); i += STEP) {
    r = libusb_bulk_transfer(
        handle,
        2, // Endpoint 2
        code.data() + i,
        std::min(STEP, static_cast<int>(code.size() - i)),
        nullptr, // Actual length transferred (not used here)
        0 // Timeout in milliseconds
    );
    if (r < 0) {
      LOGE("Failed to flash data at offset %lu: %d", i, r);
      return false;
    }
  }

  // Reset
  LOGI("flash: resetting");
  r = libusb_control_transfer(
      handle,
      LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xd8, // bRequest
      0, // wValue
      0, // wIndex
      nullptr, // Data buffer (not used here)
      0, // Length of data buffer
      15000 // Timeout in milliseconds (consider increasing for reset)
  );
  if (r < 0) {
    LOGI("Failed to reset device (%d) this was expected", r);
  }

  return true;
}

std::vector<uint8_t> firmware_signature(std::vector<uint8_t> &data) {
  return std::vector<uint8_t>(data.end()-0x80, data.end());
}

std::vector<uint8_t> get_signature(libusb_device_handle* handle) {
  std::vector<uint8_t> part_1(0x40);
  std::vector<uint8_t> part_2(0x40);

  // Read part 1 of the signature
  int bytes_received = libusb_control_transfer(
      handle,
      LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xd3, // bRequest
      0, // wValue
      0, // wIndex
      part_1.data(), // Data buffer
      part_1.size(), // Length of data buffer
      0 // Timeout in milliseconds
  );
  if (bytes_received != 0x40) {
    LOGE("Failed to read part 1 of signature");
  }

  // Read part 2 of the signature
  bytes_received = libusb_control_transfer(
      handle,
      LIBUSB_ENDPOINT_IN | LIBUSB_REQUEST_TYPE_VENDOR | LIBUSB_RECIPIENT_DEVICE,
      0xd4, // bRequest
      0, // wValue
      0, // wIndex
      part_2.data(), // Data buffer
      part_2.size(), // Length of data buffer
      0 // Timeout in milliseconds
  );
  if (bytes_received != 0x40) {
    LOGE("Failed to read part 2 of signature");
  }

  // Concatenate the two parts
  part_1.insert(part_1.end(), part_2.begin(), part_2.end());
  return part_1;
}

void hex_dump(std::vector<uint8_t>& sig) {
  static const char *hex = "0123456789abcdef";
  char out[32+1] = {0};
  char *p = out;
  for (int i=0; i<16; ++i) {
    *p++ = hex[sig[i] >> 4];
    *p++ = hex[sig[i] & 0x0f];
  }
  LOGI("Hex: %s", out);
}

extern "C" {

jint JNICALL Java_ai_flow_flowy_PythonRunner_run(JNIEnv *env, jobject obj, jint fd, jstring obj_path) {
  JavaVM* jvm;
  env->GetJavaVM(&jvm);
  libusb_context *ctx;
  libusb_device_handle *dev_handle = NULL;
  libusb_set_option(ctx, LIBUSB_OPTION_ANDROID_JAVAVM, jvm, 0);
  libusb_init(&ctx);

  open_panda(ctx, &dev_handle);
  McuConfig mcu_config = get_mcu_config(get_mcu_type(dev_handle));
  std::string path = env->GetStringUTFChars(obj_path, NULL);
  std::string fw_path = path + mcu_config.app_fn;

  std::ifstream instream(fw_path, std::ios::in | std::ios::binary);
  std::vector<uint8_t> data((std::istreambuf_iterator<char>(instream)), std::istreambuf_iterator<char>());

  std::vector<uint8_t> panda_sig = get_signature(dev_handle);
  std::vector<uint8_t> fw_sig = firmware_signature(data);

  hex_dump(panda_sig);
  hex_dump(fw_sig);

  if (panda_sig == fw_sig) {
    LOGI("Panda is up to date");
  } else {
    LOGW("Panda firmware mismatch");

    reset(dev_handle, true);

    libusb_close(dev_handle);
    dev_handle = NULL;
    libusb_exit(ctx);
    ctx = NULL;
    libusb_init(&ctx);

    open_panda(ctx, &dev_handle);

    flash_static(dev_handle, data, mcu_config);
  }

  libusb_close(dev_handle);
  dev_handle = NULL;
  libusb_exit(ctx);
  return 0;
}
}