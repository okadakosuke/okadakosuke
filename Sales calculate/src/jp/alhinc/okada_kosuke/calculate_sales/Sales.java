package jp.alhinc.okada_kosuke.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Sales {

	public static void main(String[] args) {

		String error="予期せぬエラーが発生しました";
		if(args.length !=1){
			System.out.println(error);
			return;
		}

		File branchFile = new File (args[0],  File.separator + "branch.lst" );
		File commodityFile = new File (args[0], File.separator + "commodity.lst" );

		HashMap<String, String>branchNameMap = new HashMap<String,String>();
		HashMap<String,String>commodityNameMap = new HashMap<String,String>();
		HashMap<String,Long> branchSaleMap = new HashMap<String,Long>();
		HashMap<String,Long> commoditySaleMap = new HashMap<String,Long>();

		if(!reader(branchFile , branchNameMap , branchSaleMap ,"^[0-9]{3}$" ,"支店")){
			return;
		}
		if(!reader(commodityFile ,  commodityNameMap , commoditySaleMap , "\\w{8}" , "商品")){
			return;
		}

		File rcdFile = new File (args[0]);

		File[] filecheck = rcdFile.listFiles();
		ArrayList <Integer> rcdNumber = new ArrayList <Integer>();
		ArrayList <String> rcdList = new ArrayList <String>();
		for(int i=0; i <filecheck.length; i++){
			if(filecheck[i].getName().matches("^[0-9]{8}.rcd$") && filecheck[i].isFile() ){
				String[] rcddata = filecheck[i].getName().split( "\\.");
				rcdNumber.add(Integer.valueOf(rcddata[0]) );
				rcdList.add(filecheck[i].getName());
			}
		}

		if(rcdNumber.size() != rcdNumber.get(rcdNumber.size()-1) - rcdNumber.get(0) + 1){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		BufferedReader br = null;

		try{
			for(int i=0; i <rcdList.size(); i++){
				File file = new File (args[0], rcdList.get(i) );
				ArrayList <String> rcdRead = new ArrayList <String> ();
				br = new BufferedReader(new FileReader (file) );
				String s;
				while( (s=br.readLine() ) !=null){
					rcdRead.add(s);
				}

				if (rcdRead.size() !=3){
					System.out.println(file.getName() + "のフォーマットが不正です");
					return;
				}

				if (!rcdRead.get(2).matches("^[0-9]*$") ) {
					System.out.println(error);
					return;
				}

				if (! branchSaleMap.containsKey (rcdRead.get(0)) ){
					System.out.println(file.getName() + "の支店コードが不正です");
					return;
				}
				if (! commoditySaleMap.containsKey(rcdRead.get(1)) ){
					System.out.println(file.getName() + "の商品コードが不正です");
					return;
				}

				branchSaleMap.put(rcdRead.get(0), Long.parseLong(rcdRead.get(2)) + branchSaleMap.get(rcdRead.get(0)) );
				commoditySaleMap.put(rcdRead.get(1), Long.parseLong(rcdRead.get(2)) + commoditySaleMap.get(rcdRead.get(1)) );

				int branchDigit = String.valueOf(branchSaleMap.get(rcdRead.get(0)) ).length();
				int commonDigit = String.valueOf(commoditySaleMap.get(rcdRead.get(1)) ).length();
				if(branchDigit > 10 || commonDigit > 10){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}

		}catch(IOException e){
			System.out.println(error);
			return;
		}finally{
			if(br !=null){
				try{
					br.close();
				}catch(IOException e){
					System.out.println(error);
					return;
				}
			}
		}
		File branchoutFile = new File (args[0], "branch.out");
		File commodityoutFile = new File (args[0], "commodity.out");

		if(!writer(branchSaleMap , branchNameMap , branchoutFile)){
			return;
		}
		if(!writer(commoditySaleMap , commodityNameMap , commodityoutFile)){
			return;
		}
	}

	static boolean reader(File file , Map<String , String> nameMap , Map<String , Long> saleMap , String match , String name){
		BufferedReader br = null;
		String error = "予期せぬエラーが発生しました";
		if (!file.exists() || !file.canRead() ){
			System.out.println(name + "定義ファイルが存在しません");
			return false;
		}

		try{
			br = new BufferedReader(new FileReader(file) );
			String s;

			while( (s = br.readLine() ) != null){
				String[] data = s.split(",");

				if(data.length !=2|| !data[0].matches(match) ) {
					System.out.println(name + "定義ファイルのフォーマットが不正です");
					return false;
				}
				nameMap.put(data[0], data[1]);
				saleMap.put(data[0], 0L);
			}
		}catch(IOException e){
			System.out.println(error);
			return false;
		}finally{
			if(br != null){
				try{
					br.close();
				}catch(IOException e){
					System.out.println(error);
					return false;
				}
			}
		}return true;
	}

	static boolean writer(Map<String,Long> saleMap , Map<String,String> nameMap , File file){
		BufferedWriter bw = null;
		String error = "予期せぬエラーが発生しました";
		try{
			List <Map.Entry <String,Long> > entries2 =
					new ArrayList <Map.Entry <String,Long> > (saleMap.entrySet() );
			Collections.sort(entries2, new Comparator<Map.Entry <String,Long> > () {
				public int compare(
						Entry <String,Long> entry1, Entry <String,Long> entry2) {
					return ((Long)entry2.getValue() ).compareTo((Long)entry1.getValue() );
				}
			});
			bw = new BufferedWriter ( new FileWriter(file) );
			for (Entry <String,Long> s:entries2) {

				bw.write(s.getKey() + "," + nameMap.get(s.getKey() ) + "," + s.getValue() + System.getProperty("line.separator") );
			}
		}catch(IOException e){
			System.out.println(error);
			return false;
		}finally{
			if(bw !=null){
				try{bw.close();
				}catch(IOException e){
					System.out.println(error);
					return false;
				}
			}
		}return true;
	}
}