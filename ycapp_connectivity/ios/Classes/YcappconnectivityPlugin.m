#import "YcappconnectivityPlugin.h"
#if __has_include(<ycappconnectivity/ycappconnectivity-Swift.h>)
#import <ycappconnectivity/ycappconnectivity-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "ycappconnectivity-Swift.h"
#endif

@implementation YcappconnectivityPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftYcappconnectivityPlugin registerWithRegistrar:registrar];
}
@end
