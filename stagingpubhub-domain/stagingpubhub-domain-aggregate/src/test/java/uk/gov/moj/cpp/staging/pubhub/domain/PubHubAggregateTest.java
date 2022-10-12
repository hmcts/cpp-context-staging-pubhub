package uk.gov.moj.cpp.staging.pubhub.domain;

import static java.util.stream.Collectors.toList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.staging.pubhub.HearingDates;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.StandardList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PubHubAggregateTest {
    @InjectMocks
    private PubHubAggregate pubHubAggregate;

    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private static final String STANDARD = "Standard";

    @Before
    public void setUp() {
        this.pubHubAggregate = new PubHubAggregate();
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldPublishRequestedWhenListTypeIsStandardList() {
        final StandardList standardList = StandardList.standardList()
                .withListType("public")
                .withCourtCentreName("Lavender Hill Magistrates' Court")
                .withCourtCentreAddress1("176A Lavender Hill London")
                .withHearingDates(asList(HearingDates.hearingDates()
                        .withHearingDate("2022-04-07")
                        .build()))
                .build();


        final PublishRequested publishRequested = (PublishRequested)pubHubAggregate
                .requestPublish(standardList)
                .collect(toList())
                .get(0);

        assertThat(publishRequested.getListType(), is(STANDARD));
        assertThat(publishRequested.getStandardList().getListType(), is("public"));
        assertThat(publishRequested.getStandardList().getCourtCentreName(), is("Lavender Hill Magistrates' Court"));
        assertThat(publishRequested.getStandardList().getCourtCentreAddress1(), is("176A Lavender Hill London"));
        assertThat(publishRequested.getStandardList().getHearingDates().get(0).getHearingDate(), is("2022-04-07"));
    }
}
