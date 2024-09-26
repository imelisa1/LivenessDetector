# Liveness Detector SDK

Bu SDK, basit bir arayüzle canlılık analizi yapmanıza olanak sağlar. Projenize hızlı bir şekilde entegre ederek yüz tanıma işlemlerinizde canlılık tespiti yapabilirsiniz.

## Kurulum

1. **AAR Dosyasını Projeye Ekleyin**:
   - `LivenessDetectorSDK/build/outputs/aar` dizinindeki `.aar` dosyasını, projenizin `app/libs` klasörüne kopyalayın. Eğer `libs` klasörü yoksa manuel olarak oluşturabilirsiniz.

2. **Gradle Dosyasını Güncelleyin**:
   - Projenizin `build.gradle (Module: app)` dosyasına aşağıdaki bağımlılıkları ekleyin:

     ```gradle
     implementation(name: 'LivenessDetector-release', ext: 'aar')
     implementation files('libs/javacpp-presets-platform-1.5.10')
     ```

   - Ayrıca, `.aar` dosyasının konumunu belirtmek için `repositories` kısmına aşağıdaki satırları ekleyin:

     ```gradle
     repositories {
         flatDir {
             dirs 'libs'
         }
     }
     ```

3. **Gerekli Bağımlılıkları Ekleyin**:
   - `build.gradle` dosyanıza aşağıdaki bağımlılıkları eklemeyi unutmayın:

     ```gradle
     dependencies {
         implementation 'androidx.appcompat:appcompat:1.7.0'
         implementation 'androidx.appcompat:appcompat-resources:1.7.0'
         implementation 'androidx.activity:activity:1.8.0'
         implementation 'androidx.transition:transition:1.5.0'
         implementation 'androidx.core:core-ktx:1.13.0'
         implementation 'androidx.core:core:1.13.0'
         implementation 'androidx.annotation:annotation-experimental:1.4.0'
         implementation 'com.google.android.material:material:1.12.0'
         implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
         testImplementation 'junit:junit:4.13.2'
         androidTestImplementation 'androidx.test.ext:junit:1.2.1'
         androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
         implementation 'org.bytedeco:javacv:1.5.6'
         implementation files('libs/javacpp-presets-platform-1.5.10')
         implementation "androidx.camera:camera-core:1.1.0"
         implementation "androidx.camera:camera-camera2:1.1.0"
         implementation "androidx.camera:camera-lifecycle:1.1.0"
         implementation "androidx.camera:camera-view:1.0.0-alpha23"
         implementation 'org.tensorflow:tensorflow-lite:2.8.0'
         implementation 'org.tensorflow:tensorflow-lite-support:0.3.1'
         implementation 'org.tensorflow:tensorflow-lite-gpu:2.8.0'
     }
     ```

## Kullanım

SDK'yı kullanmaya başlamak için aşağıdaki gibi bir tanımlama yapabilirsiniz:

```java
LivenessDetector livenessDetector = new LivenessDetector(this);
livenessDetector.initialize();
