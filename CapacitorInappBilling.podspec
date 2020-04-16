
  Pod::Spec.new do |s|
    s.name = 'CapacitorInappBilling'
    s.version = '0.0.1'
    s.summary = 'Support InApp Billing with capacitor'
    s.license = 'MIT'
    s.homepage = 'https://github.com/TerminusStudio/Capacitor-InApp-Billing'
    s.author = 'Terminus Studio'
    s.source = { :git => 'https://github.com/TerminusStudio/Capacitor-InApp-Billing', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end