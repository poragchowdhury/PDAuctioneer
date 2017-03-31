javac -cp commons-csv-1.0.jar:weka-stable-3.6.12.jar:src -d bin/ src/Auctioneer/PDAuctioneer.java &
java -cp bin:bin:commons-csv-1.0.jar:weka-stable-3.6.12.jar Auctioneer.PDAuctioneer &