plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.kapt)
}

android {
	namespace = "com.mul.ocv.cammat.dcscanner"
	compileSdk = 36
	
	defaultConfig {
		applicationId = "com.mul.ocv.cammat.dcscanner"
		minSdk = 24
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}
	
	buildTypes {
		release {
			isMinifyEnabled = false
//			proguardFiles(
//				getDefaultProguardFile("proguard-android-optimize.txt"),
//				"proguard-rules.pro"
//			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	kotlinOptions {
		jvmTarget = "11"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {
	
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.viewModelCompose)
	implementation(libs.androidx.ui)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(platform(libs.androidx.compose.bom))
	
	//[material3]
	implementation(libs.androidx.material3)
	
	//[ CameraX]
	implementation(libs.androidx.camera.core)
	implementation(libs.androidx.camera.view)
	implementation(libs.androidx.camera.camera2)
	implementation(libs.androidx.camera.lifecycle)
	
	//[Hilt]
//	implementation(libs.hilt.android)
//	kapt(libs.hilt.compiler)
	
	//[openCV-module]
	//implementation(project(":openCV"))
	
	implementation(files("libs/openCV-debug.aar"))
	
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}