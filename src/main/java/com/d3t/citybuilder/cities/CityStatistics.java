package com.d3t.citybuilder.cities;

import java.util.ArrayList;

import com.d3t.citybuilder.util.FiveScaleIntList;
import com.d3t.citybuilder.util.RealEstateType;
import com.d3t.citybuilder.util.ThreeScaleIntList;
import com.d3t.citybuilder.zones.RealEstateData;
import com.d3t.citybuilder.zones.Zone;

public class CityStatistics {
	
	private City city;
	
	//Indirect statistics - these are automatically calculated
	private int ageInDays;
	private FiveScaleIntList population = new FiveScaleIntList();
	private ThreeScaleIntList workplaceAvailability = new ThreeScaleIntList();
	
	public FiveScaleIntList residenceCount;
	public int retailCount;
	public int officeCount;
	public ThreeScaleIntList industryCount;
	
	public FiveScaleIntList emptyResidenceCount;
	public int emptyRetailCount;
	public int emptyOfficeCount;
	public ThreeScaleIntList emptyIndustryCount;
	
	public float employmentRate; //The city's employment rate.
	public float workplaceSaturationRate; //A value lower than 1 means that there is demand of workplaces due to unemployment. A value higher than 1 means the city is oversaturated with workplaces.
	public long moneyBalance; //The city's treasury.
	
	public CityStatistics(City city) {
		this.city = city;
	}
	
	public String getSaveString() {
		String s = "STATS:";
		return s;
		//TODO use new file system
	}
	
	public void setStartValues() {
		moneyBalance = 50000;
	}
	
	public void onDayStart() {
		ageInDays++;
		calculatePopStats();
	}
	
	public RealEstateData[] getAllRealEstate() {
		ArrayList<RealEstateData> list = new ArrayList<RealEstateData>();
		for(Zone z : city.chunks.values()) {
			if(z.realEstate != null) {
				for(RealEstateData re : z.realEstate) {
					list.add(re);
				}
			}
		}
		RealEstateData[] arr = new RealEstateData[list.size()];
		arr = list.toArray(arr);
		return arr;
	}
	
	public int getTotalPopulation() {
		return population.getTotal();
	}
	
	public int getPopulationPercentage(double percentage) {
		return (int)Math.ceil(getTotalPopulation()/percentage);
	}
	
	public int getPopulationPercentage(int stage, double percentage) {
		return (int)Math.ceil(population.get(stage)/percentage);
	}
	
	private void calculatePopStats() {
		//Re-initialize all values
		RealEstateData[] estates = getAllRealEstate();
		population = new FiveScaleIntList();
		workplaceAvailability = new ThreeScaleIntList();
		residenceCount = new FiveScaleIntList();
		retailCount = 0;
		officeCount = 0;
		industryCount = new ThreeScaleIntList();
		emptyResidenceCount = new FiveScaleIntList();
		emptyRetailCount = 0;
		emptyOfficeCount = 0;
		emptyIndustryCount = new ThreeScaleIntList();
		
		//Calculate all population, workplaces and real estates
		for(RealEstateData re : estates) {
			int scale = re.type.getScaleIndex();
			if(re.type.isResidental()) {
				residenceCount.add(scale, 1); //Add one residence to the counter
				if(re.hasTenant()) {
					//The house has tenants, add them to the population
					population.add(scale, re.residentsOrWorkplaces);
				} else {
					//The house is empty
					emptyResidenceCount.add(scale, 1);
				}
			} else if(re.type.isBusiness()) {
				if(re.type == RealEstateType.RETAIL) {
					retailCount++;
					if(re.hasTenant()) {
						//Add the workplaces
						workplaceAvailability.med += re.residentsOrWorkplaces;
					} else {
						//The shop is empty
						emptyRetailCount++;
					}
				} else if(re.type == RealEstateType.OFFICE) {
					officeCount++;
					if(re.hasTenant()) {
						//Add the workplaces
						workplaceAvailability.high += re.residentsOrWorkplaces;
					} else {
						//The office space is empty
						emptyOfficeCount++;
					}
				}
			} else if(re.type.isIndustrial()) {
				industryCount.add(scale, 1);
				if(re.hasTenant()) {
					//shift the workspace "wealth" down by one, then add them
					workplaceAvailability.add(Math.max(0, scale-1), re.residentsOrWorkplaces);
				} else {
					//The factory/farm is empty
					emptyIndustryCount.add(scale, 1);
				}
			}
		}
		
		//Calculate the employment situation
		//RESIDENTAL WEALTH TO WORKPLACE WEALTH CONVERSION:
		//RES	lowest	low		med		high	highest
		//WORK	L/M		L/M		M/H		M/H		H
		for(int cl = 0; cl < 5; cl++) {
			int workers = getPopulationPercentage(cl, 0.3D); //30% of all habitants are workers
			int targetWorkStage;
			int altWorkStage = -1;
			if(cl == 0) {
				targetWorkStage = 0;
				altWorkStage = 1;
			} else if(cl == 1) {
				targetWorkStage = 1;
				altWorkStage = 0;
			} else if(cl == 2) {
				targetWorkStage = 1;
				altWorkStage = 2;
			} else if(cl == 3) {
				targetWorkStage = 2;
				altWorkStage = 1;
			} else {
				targetWorkStage = 2;
			}
			for(int w = 0; w < workers; w++) {
				//If the target work class has a workspace available, take one
				if(workplaceAvailability.get(targetWorkStage) > 0) {
					workplaceAvailability.add(targetWorkStage, -1);
				} else if(altWorkStage >= 0 && workplaceAvailability.get(altWorkStage) > 0) {
					//Otherwise, take one from the alternative work class, if applicable
					workplaceAvailability.add(altWorkStage, -1);
				} else {
					//If there are no suitable workplaces available, place a "demand" in the target work class
					workplaceAvailability.add(targetWorkStage, -1);
				}
			}
		}
	}
}
