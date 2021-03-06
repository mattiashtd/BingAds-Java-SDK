package com.microsoft.bingads.examples.v10;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Calendar;

import com.microsoft.bingads.*;
import com.microsoft.bingads.v10.campaignmanagement.*;
import com.microsoft.bingads.customermanagement.*;
import static com.microsoft.bingads.examples.v10.AdExtensions.authorizationData;
import static com.microsoft.bingads.examples.v10.ExampleBase.outputCampaignsWithPartialErrors;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class KeywordsAds extends ExampleBase {

    static AuthorizationData authorizationData;
    static ServiceClient<ICampaignManagementService> CampaignService; 
    static ServiceClient<ICustomerManagementService> CustomerService; 
    
    /*
    private static java.lang.String UserName = "<UserNameGoesHere>";
    private static java.lang.String Password = "<PasswordGoesHere>";
    private static java.lang.String DeveloperToken = "<DeveloperTokenGoesHere>";
    private static long CustomerId = <CustomerIdGoesHere>;
    private static long AccountId = <AccountIdGoesHere>;
    */

    public static void main(java.lang.String[] args) {
   	 
        try
        {
            authorizationData = new AuthorizationData();
            authorizationData.setDeveloperToken(DeveloperToken);
            authorizationData.setAuthentication(new PasswordAuthentication(UserName, Password));
            authorizationData.setCustomerId(CustomerId);
            authorizationData.setAccountId(AccountId);
	         
            CampaignService = new ServiceClient<ICampaignManagementService>(
                    	authorizationData, 
                        ICampaignManagementService.class);
            
            CustomerService = new ServiceClient<ICustomerManagementService>(
                    authorizationData, 
                    ICustomerManagementService.class);
            
            // Determine whether you are able to add shared budgets by checking the pilot flags.

            boolean enabledForSharedBudgets = false;

            // Optionally you can find out which pilot features the customer is able to use.
            ArrayOfint featurePilotFlags = getCustomerPilotFeatures((long)authorizationData.getCustomerId());
               
            // The pilot flag value for shared budgets is 263.
            // Pilot flags apply to all accounts within a given customer.
            if (featurePilotFlags.getInts().contains(263))
            {
                enabledForSharedBudgets = true;
            }
            
            // If the customer is enabled for shared budgets, let's create a new budget and
            // share it with a new campaign.

            ArrayOfNullableOflong budgetIds = new ArrayOfNullableOflong();
            
            if (enabledForSharedBudgets)
            {
                ArrayOfBudget budgets = new ArrayOfBudget();
                Budget budget = new Budget();
                budget.setAmount(new java.math.BigDecimal(50));
                budget.setBudgetType(BudgetLimitType.DAILY_BUDGET_STANDARD);
                budget.setName("My Shared Budget " + System.currentTimeMillis());
                budgets.getBudgets().add(budget);

                budgetIds = addBudgets(budgets).getBudgetIds();
            }

            // Specify a campaign. 

            ArrayOfCampaign campaigns = new ArrayOfCampaign();
            Campaign campaign = new Campaign();
            campaign.setName("Summer Shoes " + System.currentTimeMillis());
            campaign.setDescription("Red shoes line.");
            // You must choose to set either the shared  budget ID or daily amount.
            // You can set one or the other, but you may not set both.
            campaign.setBudgetId(enabledForSharedBudgets ? budgetIds.getLongs().get(0) : 0L);
            campaign.setDailyBudget(enabledForSharedBudgets ? 0.0 : 50.0);
            campaign.setBudgetType(BudgetLimitType.DAILY_BUDGET_STANDARD);
            campaign.setTimeZone("PacificTimeUSCanadaTijuana");
            campaign.setDaylightSaving(true); 
             
            // You can set your campaign bid strategy to Enhanced CPC (EnhancedCpcBiddingScheme) 
            // and then, at any time, set an individual ad group or keyword bid strategy to 
            // Manual CPC (ManualCpcBiddingScheme).
            // For campaigns you can use either of the EnhancedCpcBiddingScheme or ManualCpcBiddingScheme objects. 
            // If you do not set this element, then ManualCpcBiddingScheme is used by default.
            campaign.setBiddingScheme(new EnhancedCpcBiddingScheme());
             
            // Used with FinalUrls shown in the text ads that we will add below.
            campaign.setTrackingUrlTemplate(
                "http://tracker.example.com/?season={_season}&promocode={_promocode}&u={lpurl}");

            campaigns.getCampaigns().add(campaign);

            // Specify an ad group. 

            ArrayOfAdGroup adGroups = new ArrayOfAdGroup();
            AdGroup adGroup = new AdGroup();
            adGroup.setName("Women's Red Shoes");
            ArrayList<AdDistribution> adDistribution = new ArrayList<AdDistribution>();
            adDistribution.add(AdDistribution.SEARCH);
            adGroup.setAdDistribution(adDistribution);
            adGroup.setBiddingModel(BiddingModel.KEYWORD);
            adGroup.setPricingModel(PricingModel.CPC);
            adGroup.setStartDate(null);
            Calendar calendar = Calendar.getInstance();
            adGroup.setEndDate(new com.microsoft.bingads.v10.campaignmanagement.Date());
            adGroup.getEndDate().setDay(31);
            adGroup.getEndDate().setMonth(12);
            adGroup.getEndDate().setYear(calendar.get(Calendar.YEAR));
            Bid searchBid = new Bid();
            searchBid.setAmount(0.09);
            adGroup.setSearchBid(searchBid);
            adGroup.setLanguage("English");
            
            // For ad groups you can use either of the InheritFromParentBiddingScheme or ManualCpcBiddingScheme objects. 
            // If you do not set this element, then InheritFromParentBiddingScheme is used by default.
            adGroup.setBiddingScheme(new ManualCpcBiddingScheme());

            //You could use a tracking template which would override the campaign level
            //tracking template. Tracking templates defined for lower level entities 
            //override those set for higher level entities.
            //In this example we are using the campaign level tracking template.
            adGroup.setTrackingUrlTemplate(null);

            adGroups.getAdGroups().add(adGroup);

            // In this example only the second keyword should succeed. The Text of the first keyword exceeds the limit,
            // and the third keyword is a duplicate of the second keyword. 

            ArrayOfKeyword keywords = new ArrayOfKeyword();

            Keyword keyword1 = new Keyword();
            keyword1.setBid(new Bid());
            keyword1.getBid().setAmount(0.47);
            keyword1.setParam2("10% Off");
            keyword1.setMatchType(MatchType.BROAD);
            keyword1.setText("Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes " +
                             "Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes " +
                             "Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes Brand-A Shoes");
            // For keywords you can use either of the InheritFromParentBiddingScheme or ManualCpcBiddingScheme objects. 
            // If you do not set this element, then InheritFromParentBiddingScheme is used by default.
            keyword1.setBiddingScheme(new InheritFromParentBiddingScheme());
            keywords.getKeywords().add(keyword1);

            Keyword keyword2 = new Keyword();
            keyword2.setBid(new Bid());
            keyword2.getBid().setAmount(0.47);
            keyword2.setParam2("10% Off");
            keyword2.setMatchType(MatchType.PHRASE);
            keyword2.setText("Brand-A Shoes");
            // For keywords you can use either of the InheritFromParentBiddingScheme or ManualCpcBiddingScheme objects. 
            // If you do not set this element, then InheritFromParentBiddingScheme is used by default.
            keyword2.setBiddingScheme(new InheritFromParentBiddingScheme());
            keywords.getKeywords().add(keyword2);

            Keyword keyword3 = new Keyword();
            keyword3.setBid(new Bid());
            keyword3.getBid().setAmount(0.47);
            keyword3.setParam2("10% Off");
            keyword3.setMatchType(MatchType.PHRASE);
            keyword3.setText("Brand-A Shoes");
            // For keywords you can use either of the InheritFromParentBiddingScheme or ManualCpcBiddingScheme objects. 
            // If you do not set this element, then InheritFromParentBiddingScheme is used by default.
            keyword3.setBiddingScheme(new InheritFromParentBiddingScheme());
            keywords.getKeywords().add(keyword3);


            // In this example only the first 3 ads should succeed. 
            // The Title of the fourth ad is empty and not valid,
            // and the fifth ad is a duplicate of the second ad. 

            ArrayOfAd ads = new ArrayOfAd();

            for(int i=0; i < 5; i++){
                TextAd textAd = new TextAd();
                textAd.setText("Huge Savings on red shoes.");
                textAd.setDisplayUrl("Contoso.com");

                // If you are currently using the Destination URL, you must upgrade to Final URLs. 
                // Here is an example of a DestinationUrl you might have used previously. 
                // textAd.setDestinationUrl("http://www.contoso.com/womenshoesale/?season=spring&promocode=PROMO123");

                // To migrate from DestinationUrl to FinalUrls for existing ads, you can set DestinationUrl
                // to an empty string when updating the ad. If you are removing DestinationUrl,
                // then FinalUrls is required.
                // textAd.setDestinationUrl("");

                // With FinalUrls you can separate the tracking template, custom parameters, and 
                // landing page URLs. 
                com.microsoft.bingads.v10.campaignmanagement.ArrayOfstring finalUrls = new com.microsoft.bingads.v10.campaignmanagement.ArrayOfstring();
                finalUrls.getStrings().add("http://www.contoso.com/womenshoesale");
                textAd.setFinalUrls(finalUrls);

                // Final Mobile URLs can also be used if you want to direct the user to a different page 
                // for mobile devices.
                com.microsoft.bingads.v10.campaignmanagement.ArrayOfstring finalMobileUrls = new com.microsoft.bingads.v10.campaignmanagement.ArrayOfstring();
                finalMobileUrls.getStrings().add("http://mobile.contoso.com/womenshoesale");
                textAd.setFinalMobileUrls(finalMobileUrls);

                // You could use a tracking template which would override the campaign level
                // tracking template. Tracking templates defined for lower level entities 
                // override those set for higher level entities.
                // In this example we are using the campaign level tracking template.
                textAd.setTrackingUrlTemplate(null);

                // Set custom parameters that are specific to this ad, 
                // and can be used by the ad, ad group, campaign, or account level tracking template. 
                // In this example we are using the campaign level tracking template.
                CustomParameters urlCustomParameters = new CustomParameters();
                CustomParameter customParameter1 = new CustomParameter();
                customParameter1.setKey("promoCode");
                customParameter1.setValue("PROMO" + (i+1));
                ArrayOfCustomParameter customParameters = new ArrayOfCustomParameter();
                customParameters.getCustomParameters().add(customParameter1);
                CustomParameter customParameter2 = new CustomParameter();
                customParameter2.setKey("season");
                customParameter2.setValue("summer");
                customParameters.getCustomParameters().add(customParameter2);
                urlCustomParameters.setParameters(customParameters);
                textAd.setUrlCustomParameters(urlCustomParameters);

                ads.getAds().add(textAd);
            }

            ((TextAd)(ads.getAds().get(0))).setTitle("Women's Shoe Sale");
            ((TextAd)(ads.getAds().get(1))).setTitle("Women's Super Shoe Sale");
            ((TextAd)(ads.getAds().get(2))).setTitle("Women's Red Shoe Sale");
            ((TextAd)(ads.getAds().get(3))).setTitle("");
            ((TextAd)(ads.getAds().get(4))).setTitle("Women's Super Shoe Sale");


            // Add the campaign, ad group, keywords, and ads

            AddCampaignsResponse addCampaignsResponse = addCampaigns(AccountId, campaigns);
            ArrayOfNullableOflong nullableCampaignIds = addCampaignsResponse.getCampaignIds();
            ArrayOfBatchError campaignErrors = addCampaignsResponse.getPartialErrors();

            AddAdGroupsResponse addAdGroupsResponse = addAdGroups(nullableCampaignIds.getLongs().get(0), adGroups);
            ArrayOfNullableOflong adGroupIds = addAdGroupsResponse.getAdGroupIds();
            ArrayOfBatchError adGroupErrors = addAdGroupsResponse.getPartialErrors();

            AddKeywordsResponse addKeywordsResponse = addKeywords(adGroupIds.getLongs().get(0), keywords);
            ArrayOfNullableOflong keywordIds = addKeywordsResponse.getKeywordIds();
            ArrayOfBatchError keywordErrors = addKeywordsResponse.getPartialErrors();

            AddAdsResponse addAdsResponse = addAds(adGroupIds.getLongs().get(0), ads);
            ArrayOfNullableOflong adIds = addAdsResponse.getAdIds();
            ArrayOfBatchError adErrors = addAdsResponse.getPartialErrors();

            // Output the new assigned entity identifiers, as well as any partial errors

            outputCampaignsWithPartialErrors(campaigns, nullableCampaignIds, campaignErrors);
            outputAdGroupsWithPartialErrors(adGroups, adGroupIds, adGroupErrors);
            outputKeywordsWithPartialErrors(keywords, keywordIds, keywordErrors);
            outputAdsWithPartialErrors(ads, adIds, adErrors);

            // Here is a simple example that updates the campaign budget.
            // If the campaign has a shared budget you cannot update the Campaign budget amount,
            // and you must instead update the amount in the Budget object. If you try to update 
            // the budget amount of a campaign that has a shared budget, the service will return 
            // the CampaignServiceCannotUpdateSharedBudget error code.
            
            ArrayList<CampaignType> campaignTypes = new ArrayList<CampaignType>();
            campaignTypes.add(CampaignType.SHOPPING);
            campaignTypes.add(CampaignType.SEARCH_AND_CONTENT);
            
            ArrayList<CampaignAdditionalField> campaignAdditionalFields = new ArrayList<CampaignAdditionalField>();
            campaignAdditionalFields.add(CampaignAdditionalField.BIDDING_SCHEME);
            campaignAdditionalFields.add(CampaignAdditionalField.BUDGET_ID);
            
            ArrayOfCampaign getCampaigns = getCampaignsByAccountId(
                authorizationData.getAccountId(),
                campaignTypes,
                campaignAdditionalFields);

            ArrayOfCampaign updateCampaigns = new ArrayOfCampaign();
            ArrayOfBudget updateBudgets = new ArrayOfBudget();
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong campaignIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong getBudgetIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
            
            // Increase existing budgets by 20%
            for (Campaign getCampaign : getCampaigns.getCampaigns())
            {
                // If the campaign has a shared budget, let's add the budget ID to the list we will update later.
                if (getCampaign != null && getCampaign.getBudgetId() != null && getCampaign.getBudgetId() > 0)
                {
                    getBudgetIds.getLongs().add((long)getCampaign.getBudgetId());
                }
                // If the campaign has its own budget, let's add it to the list of campaigns to update later.
                else if(getCampaign != null)
                {
                    updateCampaigns.getCampaigns().add(getCampaign);
                }
            }

            // Update shared budgets in Budget objects.
            if (getBudgetIds.getLongs() != null)
            {
                ArrayList<java.lang.Long> distinctBudgetIds = new ArrayList<java.lang.Long>(new HashSet<Long>(getBudgetIds.getLongs()));
                com.microsoft.bingads.v10.campaignmanagement.ArrayOflong getDistinctBudgetIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
                for(java.lang.Long id : distinctBudgetIds){
                    getDistinctBudgetIds.getLongs().add(id);
                }
                ArrayOfBudget getBudgets = getBudgetsByIds(getDistinctBudgetIds).getBudgets();

                outputStatusMessage("List of shared budgets BEFORE update:\n");
                for (Budget budget : getBudgets.getBudgets())
                {
                    outputStatusMessage("Budget:");
                    outputBudget(budget);
                }

                outputStatusMessage("List of campaigns that share each budget:\n");
                ArrayOfIdCollection getCampaignIdCollection = getCampaignIdsByBudgetIds(getDistinctBudgetIds).getCampaignIdCollection();
                for(int index = 0; index < getCampaignIdCollection.getIdCollections().size(); index++)
                {
                    outputStatusMessage(String.format("BudgetId: %s", getDistinctBudgetIds.getLongs().get(index)));
                    outputStatusMessage("Campaign Ids:");
                    if(getCampaignIdCollection.getIdCollections().get(index) != null)
                    {
                        for (java.lang.Long id : getCampaignIdCollection.getIdCollections().get(index).getIds().getLongs())
                        {
                            outputStatusMessage(String.format("\t%s", id));
                        }
                    }
                }

                for (Budget budget : getBudgets.getBudgets())
                {
                    if (budget != null)
                    {
                        // Increase budget by 20 %
                        budget.setAmount(new java.math.BigDecimal(budget.getAmount().doubleValue() * 1.2));
                        updateBudgets.getBudgets().add(budget);
                    }
                }
                updateBudgets(updateBudgets);

                getBudgets = getBudgetsByIds(getDistinctBudgetIds).getBudgets();

                outputStatusMessage("List of shared budgets AFTER update:\n");
                for (Budget budget : getBudgets.getBudgets())
                {
                    outputStatusMessage("Budget:");
                    outputBudget(budget);
                }
            }

            // Update unshared budgets in Campaign objects.
            if(updateCampaigns.getCampaigns() != null)
            {
                // The UpdateCampaigns operation only accepts 100 Campaign objects per call. 
                // To simply the example we will update the first 100.
                List<Campaign> campaignSubList = updateCampaigns.getCampaigns().subList(0, 99);
                updateCampaigns = new ArrayOfCampaign();
                for (Campaign updateCampaign : campaignSubList){
                    updateCampaigns.getCampaigns().add(updateCampaign);
                }
                
                outputStatusMessage("List of campaigns with unshared budget BEFORE budget update:\n");
                for (Campaign updateCampaign : updateCampaigns.getCampaigns())
                {
                    outputStatusMessage("Campaign:");
                    outputCampaign(updateCampaign);

                    // Monthly budgets are deprecated and there will be a forced migration to daily budgets in calendar year 2017. 
                    // Shared budgets do not support the monthly budget type, so this is only applicable to unshared budgets. 
                    // During the migration all campaign level unshared budgets will be rationalized as daily. 
                    // The formula that will be used to convert monthly to daily budgets is: Monthly budget amount / 30.4.
                    // Moving campaign monthly budget to daily budget is encouraged before monthly budgets are migrated. 

                    if (updateCampaign.getBudgetType() == BudgetLimitType.MONTHLY_BUDGET_SPEND_UNTIL_DEPLETED)
                    {
                        // Increase budget by 20 %
                        updateCampaign.setBudgetType(BudgetLimitType.DAILY_BUDGET_STANDARD);
                        updateCampaign.setDailyBudget(((updateCampaign.getMonthlyBudget() / 30.4) * 1.2));
                    }
                    else
                    {
                        // Increase budget by 20 %
                        updateCampaign.setDailyBudget(updateCampaign.getDailyBudget() * 1.2);
                    }

                    campaignIds.getLongs().add((long)updateCampaign.getId());
                }

                updateCampaigns(authorizationData.getAccountId(), updateCampaigns);
                
                
                getCampaignsByIds(authorizationData.getAccountId(),
                    campaignIds,
                    campaignTypes,
                    campaignAdditionalFields);

                outputStatusMessage("List of campaigns with unshared budget AFTER budget update:\n");
                for (Campaign getCampaign : getCampaigns.getCampaigns())
                {
                    outputStatusMessage("Campaign:");
                    outputCampaign(getCampaign);
                }
            }

            // Update the Text for the 3 successfully created ads, and update some UrlCustomParameters.

            ArrayOfAd updateAds = new ArrayOfAd();

            TextAd updateTextAd1 = new TextAd();
            updateTextAd1.setId(adIds.getLongs().get(0));
            updateTextAd1.setText("Huge Savings on All Red Shoes.");
            // Set the UrlCustomParameters element to null or empty to retain any 
            // existing custom parameters.
            CustomParameters urlCustomParameters1 = null;
            updateTextAd1.setUrlCustomParameters(urlCustomParameters1);
            updateAds.getAds().add(updateTextAd1);

            TextAd updateTextAd2 = new TextAd();
            updateTextAd2.setId(adIds.getLongs().get(1));
            updateTextAd2.setText("Huge Savings on All Red Shoes.");
            // To remove all custom parameters, set the Parameters element of the 
            // CustomParameters object to null or empty.
            CustomParameters urlCustomParameters2 = new CustomParameters();
            ArrayOfCustomParameter customParameters2 = null;
            urlCustomParameters2.setParameters(customParameters2);
            updateTextAd2.setUrlCustomParameters(urlCustomParameters2);
            updateAds.getAds().add(updateTextAd2);

            TextAd updateTextAd3 = new TextAd();
            updateTextAd3.setId(adIds.getLongs().get(2));
            updateTextAd3.setText("Huge Savings on All Red Shoes.");
            // To remove a subset of custom parameters, specify the custom parameters that 
            // you want to keep in the Parameters element of the CustomParameters object.
            CustomParameters urlCustomParameters3 = new CustomParameters();
            CustomParameter customParameter3 = new CustomParameter();
            customParameter3.setKey("promoCode");
            customParameter3.setValue("updatedpromo");
            ArrayOfCustomParameter customParameters3 = new ArrayOfCustomParameter();
            customParameters3.getCustomParameters().add(customParameter3);
            urlCustomParameters3.setParameters(customParameters3);
            updateTextAd3.setUrlCustomParameters(urlCustomParameters3);
            updateAds.getAds().add(updateTextAd3);

            // As an exercise you can step through using the debugger and view the results.

            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong updateAdGroupIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
            updateAdGroupIds.getLongs().add(adGroupIds.getLongs().get(0));

            GetAdsByAdGroupIdResponse getAdsByAdGroupIdResponse = getAdsByAdGroupId(adGroupIds.getLongs().get(0));
            UpdateAdsResponse updateAdsResponse = updateAds(adGroupIds.getLongs().get(0), updateAds);
            getAdsByAdGroupIdResponse = getAdsByAdGroupId(adGroupIds.getLongs().get(0));


            // Here is a simple example that updates the keyword bid to use the ad group bid.

            Keyword updateKeyword = new Keyword();
            updateKeyword.setId(keywordIds.getLongs().get(0));
            updateKeyword.setBid(new Bid());
            updateKeyword.getBid().setAmount(null);
            ArrayOfKeyword updateKeywords = new ArrayOfKeyword();
            updateKeywords.getKeywords().add(updateKeyword);

            // As an exercise you can step through using the debugger and view the results.

            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong updateKeywordIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
            updateKeywordIds.getLongs().add(keywordIds.getLongs().get(0));
            
            ArrayList<KeywordAdditionalField> keywordAdditionalFields = new ArrayList<KeywordAdditionalField>();
            keywordAdditionalFields.add(KeywordAdditionalField.BIDDING_SCHEME);

            GetKeywordsByAdGroupIdResponse getKeywordsByAdGroupIdResponse = getKeywordsByAdGroupId(adGroupIds.getLongs().get(0), keywordAdditionalFields);
            UpdateKeywordsResponse updateKeywordsResponse = updateKeywords(adGroupIds.getLongs().get(0), updateKeywords);
            getKeywordsByAdGroupIdResponse = getKeywordsByAdGroupId(adGroupIds.getLongs().get(0), keywordAdditionalFields);      


            // Delete the campaign, ad group, keyword, and ad that were previously added. 
            // You should remove this line if you want to view the added entities in the 
            // Bing Ads web application or another tool.

            campaignIds = new com.microsoft.bingads.v10.campaignmanagement.ArrayOflong();
            campaignIds.getLongs().add(nullableCampaignIds.getLongs().get(0));
            deleteCampaigns(AccountId, campaignIds);
            System.out.printf("Deleted CampaignId %d\n", nullableCampaignIds.getLongs().get(0));

            outputStatusMessage("Program execution completed\n"); 

        // Campaign Management service operations can throw AdApiFaultDetail.
        } catch (com.microsoft.bingads.v10.campaignmanagement.AdApiFaultDetail_Exception ex) {
            outputStatusMessage("The operation failed with the following faults:\n");

            for (com.microsoft.bingads.v10.campaignmanagement.AdApiError error : ex.getFaultInfo().getErrors().getAdApiErrors())
            {
                outputStatusMessage("AdApiError\n");
                outputStatusMessage(String.format("Code: %d\nError Code: %s\nMessage: %s\n\n", 
                                error.getCode(), error.getErrorCode(), error.getMessage()));
            }

        // Campaign Management service operations can throw ApiFaultDetail.
        } catch (com.microsoft.bingads.v10.campaignmanagement.ApiFaultDetail_Exception ex) {
            outputStatusMessage("The operation failed with the following faults:\n");

            for (com.microsoft.bingads.v10.campaignmanagement.BatchError error : ex.getFaultInfo().getBatchErrors().getBatchErrors())
            {
                outputStatusMessage(String.format("BatchError at Index: %d\n", error.getIndex()));
                outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
            }

            for (com.microsoft.bingads.v10.campaignmanagement.OperationError error : ex.getFaultInfo().getOperationErrors().getOperationErrors())
            {
                outputStatusMessage("OperationError\n");
                outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
            }

        // Some Campaign Management service operations such as SetAdExtensionsAssociations can throw EditorialApiFaultDetail.
        } catch (com.microsoft.bingads.v10.campaignmanagement.EditorialApiFaultDetail_Exception ex) {
            outputStatusMessage("The operation failed with the following faults:\n");

            for (com.microsoft.bingads.v10.campaignmanagement.BatchError error : ex.getFaultInfo().getBatchErrors().getBatchErrors())
            {
                outputStatusMessage(String.format("BatchError at Index: %d\n", error.getIndex()));
                outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
            }

            for (com.microsoft.bingads.v10.campaignmanagement.EditorialError error : ex.getFaultInfo().getEditorialErrors().getEditorialErrors())
            {
                outputStatusMessage(String.format("EditorialError at Index: %d\n\n", error.getIndex()));
                outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
                outputStatusMessage(String.format("Appealable: %s\nDisapproved Text: %s\nCountry: %s\n\n", 
                                error.getAppealable(), error.getDisapprovedText(), error.getPublisherCountry()));
            }

            for (com.microsoft.bingads.v10.campaignmanagement.OperationError error : ex.getFaultInfo().getOperationErrors().getOperationErrors())
            {
                outputStatusMessage("OperationError\n");
                outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
            }
            
        // Customer Management service operations can throw AdApiFaultDetail.
        } catch (com.microsoft.bingads.customermanagement.AdApiFaultDetail_Exception ex) {
            outputStatusMessage("The operation failed with the following faults:\n");

            for (com.microsoft.bingads.customermanagement.AdApiError error : ex.getFaultInfo().getErrors().getAdApiErrors())
            {
	            outputStatusMessage("AdApiError\n");
	            outputStatusMessage(String.format("Code: %d\nError Code: %s\nMessage: %s\n\n", error.getCode(), error.getErrorCode(), error.getMessage()));
            }
        
        // Customer Management service operations can throw ApiFault.
        } catch (com.microsoft.bingads.customermanagement.ApiFault_Exception ex) {
            outputStatusMessage("The operation failed with the following faults:\n");

            for (com.microsoft.bingads.customermanagement.OperationError error : ex.getFaultInfo().getOperationErrors().getOperationErrors())
            {
	            outputStatusMessage("OperationError\n");
	            outputStatusMessage(String.format("Code: %d\nMessage: %s\n\n", error.getCode(), error.getMessage()));
            }
        } catch (RemoteException ex) {
            outputStatusMessage("Service communication error encountered: ");
            outputStatusMessage(ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            outputStatusMessage("Error encountered: ");
            outputStatusMessage(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    // Gets the list of pilot features that the customer is able to use.
    
    static ArrayOfint getCustomerPilotFeatures(java.lang.Long customerId) throws RemoteException, Exception 
    {       
		
        final GetCustomerPilotFeaturesRequest getCustomerPilotFeaturesRequest = new GetCustomerPilotFeaturesRequest();
        getCustomerPilotFeaturesRequest.setCustomerId(customerId);
        
        return CustomerService.getService().getCustomerPilotFeatures(getCustomerPilotFeaturesRequest).getFeaturePilotFlags();
    }
    
    // Adds one or more budgets that can be shared by campaigns in the account.

    static AddBudgetsResponse addBudgets(ArrayOfBudget budgets) throws RemoteException, Exception
    {
        AddBudgetsRequest request = new AddBudgetsRequest();

        request.setBudgets(budgets);
        
        return CampaignService.getService().addBudgets(request);
    }
    
    // Gets the specified budgets from the account's shared budget library.

    static GetBudgetsByIdsResponse getBudgetsByIds(
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong budgetIds) throws RemoteException, Exception
    {
        GetBudgetsByIdsRequest request = new GetBudgetsByIdsRequest();

        request.setBudgetIds(budgetIds);
        
        return CampaignService.getService().getBudgetsByIds(request);
    }
    
    // Gets the identifiers of campaigns that share each specified budget.

    static GetCampaignIdsByBudgetIdsResponse getCampaignIdsByBudgetIds(
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong budgetIds) throws RemoteException, Exception
    {
        GetCampaignIdsByBudgetIdsRequest request = new GetCampaignIdsByBudgetIdsRequest();

        request.setBudgetIds(budgetIds);
        
        return CampaignService.getService().getCampaignIdsByBudgetIds(request);
    }
    
    // Updates one or more budgets that can be shared by campaigns in the account.

    static UpdateBudgetsResponse updateBudgets(ArrayOfBudget budgets) throws RemoteException, Exception
    {
        UpdateBudgetsRequest request = new UpdateBudgetsRequest();

        request.setBudgets(budgets);
        
        return CampaignService.getService().updateBudgets(request);
    }
    
    // Deletes one or more budgets.

    static void deleteBudgets(
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong budgetIds) throws RemoteException, Exception
    {
        DeleteBudgetsRequest request = new DeleteBudgetsRequest();

        request.setBudgetIds(budgetIds);
        
        CampaignService.getService().deleteBudgets(request);
    }

    // Adds one or more campaigns to the specified account.

    static AddCampaignsResponse addCampaigns(long accountId, ArrayOfCampaign campaigns) throws RemoteException, Exception
    {
        AddCampaignsRequest request = new AddCampaignsRequest();

        request.setAccountId(accountId);
        request.setCampaigns(campaigns);

        return CampaignService.getService().addCampaigns(request);
    }

    // Deletes one or more campaigns from the specified account.

    static void deleteCampaigns(
            long accountId, 
            com.microsoft.bingads.v10.campaignmanagement.ArrayOflong campaignIds) throws RemoteException, Exception
    {
        DeleteCampaignsRequest request = new DeleteCampaignsRequest();

        request.setAccountId(accountId);
        request.setCampaignIds(campaignIds);

        CampaignService.getService().deleteCampaigns(request);
    }

    // Retrieves all the requested campaign types in the account.

    static ArrayOfCampaign getCampaignsByAccountId(
                long accountId, 
                ArrayList<CampaignType> campaignType,
                ArrayList<CampaignAdditionalField> returnAdditionalFields) throws RemoteException, Exception
    {
        GetCampaignsByAccountIdRequest request = new GetCampaignsByAccountIdRequest();

        request.setAccountId(accountId);
        request.setCampaignType(campaignType);
        request.setReturnAdditionalFields(returnAdditionalFields);

        return CampaignService.getService().getCampaignsByAccountId(request).getCampaigns();
    }
    
    // Gets one or more campaigns for the specified campaign identifiers.

    static ArrayOfCampaign getCampaignsByIds(
                long accountId, 
                com.microsoft.bingads.v10.campaignmanagement.ArrayOflong campaignIds,
                ArrayList<CampaignType> campaignType,
                ArrayList<CampaignAdditionalField> returnAdditionalFields) throws RemoteException, Exception
    {
        GetCampaignsByIdsRequest request = new GetCampaignsByIdsRequest();

        request.setAccountId(accountId);
        request.setCampaignIds(campaignIds);
        request.setCampaignType(campaignType);
        request.setReturnAdditionalFields(returnAdditionalFields);

        return CampaignService.getService().getCampaignsByIds(request).getCampaigns();
    }

    // Updates one or more campaigns.

    static void updateCampaigns(long accountId, ArrayOfCampaign campaigns) throws RemoteException, Exception
    {
        UpdateCampaignsRequest request = new UpdateCampaignsRequest();

        request.setAccountId(accountId);
        request.setCampaigns(campaigns);

        CampaignService.getService().updateCampaigns(request);
    }

    // Adds one or more ad groups to the specified campaign.

    static AddAdGroupsResponse addAdGroups(long campaignId, ArrayOfAdGroup adGroups) throws RemoteException, Exception
    {
        AddAdGroupsRequest request = new AddAdGroupsRequest();

        request.setCampaignId(campaignId);
        request.setAdGroups(adGroups);

        return CampaignService.getService().addAdGroups(request);
    }

    // Adds one or more keywords to the specified ad group.

    static AddKeywordsResponse addKeywords(long adGroupId, ArrayOfKeyword keywords) throws RemoteException, Exception
    {
        AddKeywordsRequest request = new AddKeywordsRequest();

        request.setAdGroupId(adGroupId);
        request.setKeywords(keywords);

        return CampaignService.getService().addKeywords(request);
    }

    // Adds one or more ads to the specified ad group.

    static AddAdsResponse addAds(long adGroupId, ArrayOfAd ads) throws RemoteException, Exception
    {
        AddAdsRequest request = new AddAdsRequest();

        request.setAdGroupId(adGroupId);
        request.setAds(ads);

        return CampaignService.getService().addAds(request);
    }

    // Gets the ads in the specified ad group.

    static GetAdsByAdGroupIdResponse getAdsByAdGroupId(long adGroupId) throws RemoteException, Exception
    {
        GetAdsByAdGroupIdRequest request = new GetAdsByAdGroupIdRequest();

        request.setAdGroupId(adGroupId);

        return CampaignService.getService().getAdsByAdGroupId(request);
    }

    // Updates the ads in the specified ad group.

    static UpdateAdsResponse updateAds(long adGroupId, ArrayOfAd ads) throws RemoteException, Exception
    {
        UpdateAdsRequest request = new UpdateAdsRequest();

        request.setAdGroupId(adGroupId);
        request.setAds(ads);

        return CampaignService.getService().updateAds(request);
    }

    // Gets the keywords in the specified ad group.

    static GetKeywordsByAdGroupIdResponse getKeywordsByAdGroupId(
            long adGroupId, 
            ArrayList<KeywordAdditionalField> returnAdditionalFields) throws RemoteException, Exception
    {
        GetKeywordsByAdGroupIdRequest request = new GetKeywordsByAdGroupIdRequest();

        request.setAdGroupId(adGroupId);
        request.setReturnAdditionalFields(returnAdditionalFields);

        return CampaignService.getService().getKeywordsByAdGroupId(request);
    }

    // Updates the keywords in the specified ad group.

    static UpdateKeywordsResponse updateKeywords(long adGroupId, ArrayOfKeyword keywords) throws RemoteException, Exception
    {
        UpdateKeywordsRequest request = new UpdateKeywordsRequest();

        request.setAdGroupId(adGroupId);
        request.setKeywords(keywords);

        return CampaignService.getService().updateKeywords(request);
    }
 }