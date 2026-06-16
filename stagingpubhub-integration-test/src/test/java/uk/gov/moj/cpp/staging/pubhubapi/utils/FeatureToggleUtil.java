package uk.gov.moj.cpp.staging.pubhubapi.utils;

import uk.gov.moj.cpp.platform.test.feature.toggle.FeatureStubber;

import com.google.common.collect.ImmutableMap;

public class FeatureToggleUtil {

    public static final String STAGINGPUBHUB_CONTEXT = "stagingpubhub";

    public static final void enablePubHubFeature(final boolean enabled) {
        final ImmutableMap<String, Boolean> features = ImmutableMap.of("PUBHUB", enabled);
        FeatureStubber.stubFeaturesFor(STAGINGPUBHUB_CONTEXT, features);
    }
}
