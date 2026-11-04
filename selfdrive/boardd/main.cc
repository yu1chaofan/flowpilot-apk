#include <cassert>

#include "selfdrive/boardd/boardd.h"
#include "common/swaglog.h"
#include "common/util.h"
#include "system/hardware/hw.h"

#include <jni.h>

int main(int argc, char *argv[]) {
  LOGW("starting boardd");

  int err;
  err = util::set_realtime_priority(54);
  err = util::set_core_affinity({4});
  
  std::vector<std::string> usb_devs(argv + 1, argv + argc);
  bool int_fd = usb_devs.size() != 0; // if no arguements are provided through termux-usb, fds cannot be used. 
  for (std::string usb_dev : usb_devs){
    if (usb_dev.length() > 5){ // TODO: is this ok to differentiate between serials and fds ?
      int_fd = false;
      break;
    }
  }
  
  if (int_fd){
    int fd;
    assert((argc > 1) && (sscanf(argv[1], "%d", &fd) == 1));
    boardd_main_thread(fd);
  }
  else{
    boardd_main_thread(usb_devs);
  }
  return 0;
}

extern "C" {
JNIEXPORT void Java_ai_flow_flowy_ServicePandad_nativeStart(JNIEnv* env, jclass cls, jint fd) {
  boardd_main_thread(fd);
}

JNIEXPORT void Java_ai_flow_flowy_ServicePandad_nativeStop(JNIEnv* env, jclass cls) {
  boardd_main_exit();
}
}
