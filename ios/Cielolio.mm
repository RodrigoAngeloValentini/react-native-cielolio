#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(Cielolio, NSObject)

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

@end
