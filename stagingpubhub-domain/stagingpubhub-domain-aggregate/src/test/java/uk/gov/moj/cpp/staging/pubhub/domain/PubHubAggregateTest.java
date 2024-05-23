package uk.gov.moj.cpp.staging.pubhub.domain;

import static java.util.stream.Collectors.toList;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.staging.pubhub.HearingDates;
import uk.gov.justice.staging.pubhub.Language;
import uk.gov.justice.staging.pubhub.ListPayload;
import uk.gov.justice.staging.pubhub.PressTransparencyReportGenerated;
import uk.gov.justice.staging.pubhub.PublishRequested;
import uk.gov.justice.staging.pubhub.ReadyCases;
import uk.gov.justice.staging.pubhub.RequestType;
import uk.gov.justice.staging.pubhub.StandardList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PubHubAggregateTest {
    private static final String STANDARD = "Standard";
    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private PubHubAggregate pubHubAggregate;

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


        final PublishRequested publishRequested = (PublishRequested) pubHubAggregate
                .requestPublish(standardList)
                .collect(toList())
                .get(0);

        assertThat(publishRequested.getListType(), is(STANDARD));
        assertThat(publishRequested.getStandardList().getListType(), is("public"));
        assertThat(publishRequested.getStandardList().getCourtCentreName(), is("Lavender Hill Magistrates' Court"));
        assertThat(publishRequested.getStandardList().getCourtCentreAddress1(), is("176A Lavender Hill London"));
        assertThat(publishRequested.getStandardList().getHearingDates().get(0).getHearingDate(), is("2022-04-07"));
    }

    @Test
    public void shouldRequestPressTransparencyReportGeneratedWhenListTypeIsSjpPressList() {
        final ListPayload listPayload = ListPayload.listPayload()
                .withGeneratedDateAndTime("2016-01-01 00:00:00")
                .withTotalNumberOfRecords(1)
                .withReadyCases(asList(new ReadyCases.Builder()
                        .withCaseUrn("TFL901845675")
                        .withDefendantName("John Doe")
                        .build()))
                .build();

        final PressTransparencyReportGenerated pressTransparencyReportGenerated = (PressTransparencyReportGenerated) pubHubAggregate
                .requestSjpPressPublish(Language.ENGLISH.toString(), listPayload, RequestType.FULL.toString())
                .collect(toList())
                .get(0);

        assertThat(pressTransparencyReportGenerated.getLanguage(), is(Language.ENGLISH));
        assertThat(pressTransparencyReportGenerated.getListPayload().getTotalNumberOfRecords(), is(1));
        assertThat(pressTransparencyReportGenerated.getListPayload().getReadyCases().get(0).getCaseUrn(), is("TFL901845675"));
    }
}
