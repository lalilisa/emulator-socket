package com.ftl.collector.utils;

import com.ftl.common.constants.*;
import com.ftl.common.model.market.*;
import com.ftl.common.utils.BeanUtil;
import com.ftl.common.utils.FtlDateUtils;
import com.ftl.common.utils.FtlNumberUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MsgUtils {
    private static final Logger log = LoggerFactory.getLogger(MsgUtils.class);

    public static List<SymbolInfo> parseListMsg4(String message) {
        String[] listSymbolStr = message.split("\\$");
        List<SymbolInfo> symbolInfoList = new ArrayList<>();
        for (String symbolInfoStr : listSymbolStr) {
            SymbolInfo symbolInfo = MsgUtils.parseMsg4(symbolInfoStr);
            if (symbolInfo != null) {
                symbolInfo.setTi(System.currentTimeMillis());
                symbolInfoList.add(symbolInfo);
            }
        }
        return symbolInfoList;
    }

    public static List<SymbolInfo> parseListMsg5(String message) {
        String[] listSymbolStr = message.split("\\$");
        List<SymbolInfo> symbolInfoList = new ArrayList<>();
        Set<String> indexUse = new HashSet<>();
        indexUse.add("VNINDEX");
        indexUse.add("VN30");
        indexUse.add("HNXINDEX");
        indexUse.add("HNX30");
        indexUse.add("HNXUPCOMINDEX");
        for (String symbolInfoStr : listSymbolStr) {
            SymbolInfo symbolInfo = MsgUtils.parseMsg5(symbolInfoStr);
            if (symbolInfo != null && indexUse.contains(symbolInfo.getS())) {
                symbolInfo.setTi(System.currentTimeMillis());
                symbolInfoList.add(symbolInfo);
            }
        }
        return symbolInfoList;
    }

    public static List<BidOffer> parseListMsg8(String message) {
        String[] listBidOfferStr = message.split("\\$");
        List<BidOffer> bidOfferList = new ArrayList<>();
        for (String bidOfferStr : listBidOfferStr) {
            BidOffer bidOffer = MsgUtils.parseMsg8(bidOfferStr);
            if (bidOffer != null) {
                bidOfferList.add(bidOffer);
            }
        }
        return bidOfferList;
    }

    public static List<MarketStatus> parseListMsg7(String message) {
        String[] listMarketStatusStr = message.split("\\$");
        List<MarketStatus> marketStatusList = new ArrayList<>();
        for (String marketStatusStr : listMarketStatusStr) {
            MarketStatus marketStatus = MsgUtils.parseMsg7(marketStatusStr);
            if (marketStatus != null) {
                marketStatus.setTi(System.currentTimeMillis());
                marketStatusList.add(marketStatus);
            }
        }
        return marketStatusList;
    }

    public static SymbolStatic parseMsg2(String symbolInfoStr) {
        try {
            SymbolStatic symbolInfo = new SymbolStatic();
            String[] fields = symbolInfoStr.split("\\|");
            symbolInfo.setS(fields[0]);

            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 1) {
                    continue;
                }
                String value = null;
                if (properties.length >= 2) {
                    value = properties[1];
                }
                String tag = properties[0];
                if (tag.equals("1")) {
                    symbolInfo.setCe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("2")) {
                    symbolInfo.setFl(parseToDouble(value));
                    continue;
                }
                if (tag.equals("3")) {
                    symbolInfo.setRe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("34")) {
                    symbolInfo.setT(SymbolTypeEnum.STOCK);
                    symbolInfo.setM(MarketEnum.HOSE);

                    if ("STX".equals(value)) {
                        symbolInfo.setM(MarketEnum.HNX);
                    } else if ("UPX".equals(value)) {
                        symbolInfo.setM(MarketEnum.UPCOM);
                    } else if ("DVX".equals(value)) { // phai sinh
                        symbolInfo.setM(MarketEnum.HNX);
                        symbolInfo.setT(SymbolTypeEnum.FUTURES);
                    } else if ("HCX".equals(value)) {
                        symbolInfo.setM(MarketEnum.HNX);
                        symbolInfo.setT(SymbolTypeEnum.BOND);
                    }
                    continue;
                }
                if (tag.equals("44")) {
                    /*
                    Trạng thái GD mã ck.
                      - 0: Bình thường
                      - 1: Không giao dịch
                      - 2: Nhưng giao dịch
                      - 6: Hủy niêm yết
                      - 7: Niêm yết mới
                      - 8: Sắp hủy niêm yết
                      - 10: Tạm ngừng giao dịch
                      - 25: Giao dịch đặc biệt
                     */
                    symbolInfo.setSt(StringUtils.trim(value));
                    continue;
                }
                if (tag.equals("45")) {
                    if ("ST".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.STOCK);
                    } else if ("BO".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.BOND);
                    } else if ("EF".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.ETF);
                    } else if ("FU".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.FUTURES);
                    } else if ("CW".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.CW);
                    } else if ("MF".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.FUND);
                    } else if ("OP".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.OPTION);
                    } else {
                        log.warn("invalid securities type: {}", value);
                        return null;
                    }
                    // không tìm thấy field SecurityType để map
                /*Loại chứng khoán.
                    - ST: Cổ phiếu
                    - BO: Trái phiếu
                    - MF: Chứng chỉ quỹ
                    - EF: ETF
                    - FU: Hợp đồng PS tương lai
                    - OP: Hợp đồng PS quyền chọn
                    - CW: Chứng quyền
                 */
                    continue;
                }
                if (tag.equals("46")) {
                    if ("01".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("03".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.INTERMISSION.name());
                    } else if ("10".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("30".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.LO.name());
                    } else if ("40".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATC.name());
                    } else if ("60".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PLO.name());
                    } else if ("61".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PUT_THROUGH.name());
                    } else {
                        symbolInfo.setSs("CLOSED");
                    }
                /*Trạng thái phiên giao dịch.
                      - 01: Mở cửa
                      - 03: Tạm ngưng
                      - 10: ATO
                      - 30: Liên tục
                      - 40: ATC
                      - 60: PLO
                      - 61: Thỏa thuận
                      - 90: Kết thúc nhận lệnh
                      - 96: Kết thúc ngày
                */
                    continue;
                }
                if (tag.equals("65")) {
                    if (StringUtils.isBlank(value) || StringUtils.trim(value).equals("0")) {
                        symbolInfo.setIe(null);
                    } else {
                        symbolInfo.setIe(true);
                    }
                /*
                Trạng thái thực hiện quyền.
                  - 0: Bình thường
                  - 1: Trả CT bằng tiền
                  - 2: Trả cổ tức bằng CP/CP thưởng
                  - 8: Trả cổ tức bằng tiền, trả cổ tức bằng CP/CP thưởng
                  - 3: Phát hành CP cho cổ đông hiện hữu
                  - 4: Trả cổ tức bằng CP/CP thưởng,phát hành CP cho cổ đông hiện hữu
                  - 5: Trả cổ tức bằng tiền, bằng CP/CP thưởng, phát hành CP cho cổ đông hiện hữu
                  - 9: Trả cổ tức bằng tiền, phát hành CP cho cổ đông hiện hữu
                  - 6: Niêm yết bổ sung
                  - 7: Giảm vốn
                  - 10: Thay đổi tỷ lệ Free Float
                  - 11: Họp đại cổ đông
                  - 15: Chia tách cổ phiếu
                 */
                    continue;
                }
                if (tag.equals("89")) {
                    symbolInfo.setBe(parseToDouble(value));
                }

            }
            return symbolInfo;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }


    public static SymbolInfo parseMsg4(String symbolInfoStr) {
        try {
            SymbolInfo symbolInfo = new SymbolInfo();
            String[] fields = symbolInfoStr.split("\\|");
            symbolInfo.setS(fields[0]);
            List<BidOfferItem> bb = new ArrayList<>(); // bid
            List<BidOfferItem> bo = new ArrayList<>(); // ask

            BidOfferItem bestBid1 = new BidOfferItem();
            BidOfferItem bestBid2 = new BidOfferItem();
            BidOfferItem bestBid3 = new BidOfferItem();
            BidOfferItem bestAsk1 = new BidOfferItem();
            BidOfferItem bestAsk2 = new BidOfferItem();
            BidOfferItem bestAsk3 = new BidOfferItem();

            bb.add(bestBid1);
            bb.add(bestBid2);
            bb.add(bestBid3);

            bo.add(bestAsk1);
            bo.add(bestAsk2);
            bo.add(bestAsk3);

            List<BidOfferItem> bbOd = new ArrayList<>(); // bid oddlot
            List<BidOfferItem> boOd = new ArrayList<>(); // ask oddlot

            BidOfferItem bestBidOd1 = new BidOfferItem();
            BidOfferItem bestBidOd2 = new BidOfferItem();
            BidOfferItem bestBidOd3 = new BidOfferItem();
            BidOfferItem bestAskOd1 = new BidOfferItem();
            BidOfferItem bestAskOd2 = new BidOfferItem();
            BidOfferItem bestAskOd3 = new BidOfferItem();

            bbOd.add(bestBidOd1);
            bbOd.add(bestBidOd2);
            bbOd.add(bestBidOd3);

            boOd.add(bestAskOd1);
            boOd.add(bestAskOd2);
            boOd.add(bestAskOd3);

            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 1) {
                    continue;
                }
                String value = null;
                if (properties.length >= 2) {
                    value = properties[1];
                }
                String tag = properties[0];
                if (tag.equals("1")) {
                    symbolInfo.setCe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("2")) {
                    symbolInfo.setFl(parseToDouble(value));
                    continue;
                }
                if (tag.equals("3")) {
                    symbolInfo.setRe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("4")) {
                    bestBid3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("5")) {
                    bestBid3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("6")) {
                    bestBid2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("7")) {
                    bestBid2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("8")) {
                    bestBid1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("9")) {
                    bestBid1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("10")) {
                    bestAsk3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("11")) {
                    bestAsk3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("12")) {
                    bestAsk2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("13")) {
                    bestAsk2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("14")) {
                    bestAsk1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("15")) {
                    bestAsk1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("16")) {
                    symbolInfo.setC(parseToDouble(value));
                    continue;
                }
                if (tag.equals("17")) {
                    symbolInfo.setCh(parseToDouble(value));
                    continue;
                }
                if (tag.equals("18")) {
                    symbolInfo.setR(FtlNumberUtil.round4Decimal(parseToDouble(value) / 100));
                    continue;
                }
                if (tag.equals("19")) {
                    symbolInfo.setMv(parseToLong(value));
                    continue;
                }
                if (tag.equals("20")) {
//                    symbolInfo.setVo(parseToDouble(value)); // cac bang gia khac dang hien vo không bao gồm giao dịch thỏa thuận
                    continue;
                }
                if (tag.equals("21")) {
//                    symbolInfo.setVa(parseToDouble(value));  // cac bang gia khac dang hien va không bao gồm giao dịch thỏa thuận
                    continue;
                }
                if (tag.equals("22")) {
                    symbolInfo.setH(parseToDouble(value));
                    continue;
                }
                if (tag.equals("23")) {
                    symbolInfo.setA(parseToDouble(value));
                    continue;
                }
                if (tag.equals("24")) {
                    symbolInfo.setL(parseToDouble(value));
                    continue;
                }
                if (tag.equals("25")) {
                    symbolInfo.setFrBvo(parseToLong(value));
                    continue;
                }
                if (tag.equals("26")) {
                    symbolInfo.setFrBva(parseToDouble(value));
                    continue;
                }
                if (tag.equals("27")) {
                    symbolInfo.setFrSvo(parseToLong(value));
                    continue;
                }
                if (tag.equals("28")) {
                    symbolInfo.setFrSva(parseToDouble(value));
                    continue;
                }
                if (tag.equals("29")) {
                    symbolInfo.setFrCr(parseToLong(value));
                    continue;
                }
                if (tag.equals("30")) {
                    symbolInfo.setO(parseToDouble(value));
                    continue;
                }
//                if (tag.equals("31")) {
//                    symbolInfo.setC(parseToDouble(value));
//                    continue;
//                }
                if (tag.equals("32")) {
                    symbolInfo.setOi(parseToDouble(value));
                    continue;
                }
                if (tag.equals("33")) {
                    // todo: OpenInterestChange: Dùng cho PS
                    continue;
                }
                if (tag.equals("34")) {
                    symbolInfo.setM(MarketEnum.HOSE);

                    if ("STX".equals(value)) {
                        symbolInfo.setM(MarketEnum.HNX);
                    } else if ("UPX".equals(value)) {
                        symbolInfo.setM(MarketEnum.UPCOM);
                    } else if ("DVX".equals(value)) { // phai sinh
                        symbolInfo.setM(MarketEnum.HNX);
                        symbolInfo.setT(SymbolTypeEnum.FUTURES);
                    } else if ("HCX".equals(value)) {
                        symbolInfo.setM(MarketEnum.HNX);
                        symbolInfo.setT(SymbolTypeEnum.BOND);
                    }
                    continue;
                }
                if (tag.equals("39")) {
                    symbolInfo.setExp(parseToDouble(value));
                    continue;
                }
                if (tag.equals("40")) {
                    symbolInfo.setExr(value);
                    continue;
                }
                if (tag.equals("41")) {
                    symbolInfo.setB(value);
                    continue;
                }
                if (tag.equals("42")) {
                    // todo: UnderlyingPrice: Dùng cho PS
                    continue;
                }
                if (tag.equals("44")) {
                    /*
                    Trạng thái GD mã ck.
                      - 0: Bình thường
                      - 1: Không giao dịch
                      - 2: Nhưng giao dịch
                      - 6: Hủy niêm yết
                      - 7: Niêm yết mới
                      - 8: Sắp hủy niêm yết
                      - 10: Tạm ngừng giao dịch
                      - 25: Giao dịch đặc biệt
                     */
                    symbolInfo.setSt(StringUtils.trim(value));
                    continue;
                }
                if (tag.equals("45")) {
                    if ("ST".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.STOCK);
                    } else if ("BO".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.BOND);
                    } else if ("EF".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.ETF);
                    } else if ("FU".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.FUTURES);
                    } else if ("CW".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.CW);
                    } else if ("MF".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.FUND);
                    } else if ("OP".equals(value)) {
                        symbolInfo.setT(SymbolTypeEnum.OPTION);
                    } else {
                        log.warn("invalid securities type: {}", value);
                        return null;
                    }
                    // không tìm thấy field SecurityType để map
                /*Loại chứng khoán.
                    - ST: Cổ phiếu
                    - BO: Trái phiếu
                    - MF: Chứng chỉ quỹ
                    - EF: ETF
                    - FU: Hợp đồng PS tương lai
                    - OP: Hợp đồng PS quyền chọn
                    - CW: Chứng quyền
                 */
                    continue;
                }
                if (tag.equals("46")) {
                    if ("01".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("03".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.INTERMISSION.name());
                    } else if ("10".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("30".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.LO.name());
                    } else if ("40".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATC.name());
                    } else if ("60".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PLO.name());
                    } else if ("61".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PUT_THROUGH.name());
                    } else {
                        symbolInfo.setSs("CLOSED");
                    }
                /*Trạng thái phiên giao dịch.
                      - 01: Mở cửa
                      - 03: Tạm ngưng
                      - 10: ATO
                      - 30: Liên tục
                      - 40: ATC
                      - 60: PLO
                      - 61: Thỏa thuận
                      - 90: Kết thúc nhận lệnh
                      - 96: Kết thúc ngày
                */
                    continue;
                }
//                if (tag.equals("47")) {
//                    symbolInfo.setN1(value);
//                    continue;
//                }
//                if (tag.equals("48")) {
//                    symbolInfo.setN2(value);
//                    continue;
//                }
                if (tag.equals("49")) {
                    symbolInfo.setTbo(parseToLong(value));
                    continue;
                }
                if (tag.equals("50")) {
                    symbolInfo.setToo(parseToLong(value));
                    continue;
                }
                if (tag.equals("51")) {
                    // todo: SubForeignQtty: KL ròng NĐT nước ngoài
                    continue;
                }
                if (tag.equals("52")) {
                    // todo: SubForeignValue: GT ròng NĐT nước ngoài
                    continue;
                }
                if (tag.equals("53")) {
                    // todo: ChangeTotalTradedQtty: Thay đối KLGD so với ngày giao dịch trước
                    continue;
                }
                if (tag.equals("54")) {
//                    symbolInfo.setAv10b(parseToDouble(value));
                    continue;
                }
                if (tag.equals("55")) {
                    // todo: TotalBuyQtty: KLGD khớp lệnh mua
                    continue;
                }
                if (tag.equals("56")) {
                    // todo: TotalBuyValue: GTGD khớp lệnh mua
                    continue;
                }
                if (tag.equals("57")) {
                    // todo: TotalSellQtty: KLGD khớp lệnh bán
                    continue;
                }
                if (tag.equals("58")) {
                    // todo TotalSellValue: GTGD khớp lệnh bán
                    continue;
                }
                if (tag.equals("60")) {
                    symbolInfo.setBa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("59")) {
                    symbolInfo.setN1(value);
                    symbolInfo.setN2(value);
                    continue;
                }
                if (tag.equals("61")) {
                    // mô tả: KLGD khớp lệnh (không bao gồm giao dịch thỏa thuận)
                    symbolInfo.setVo(parseToDouble(value));
                    continue;
                }
                if (tag.equals("62")) {
                    // mô tả: GTGD khớp lệnh (không bao gồm giao dich thỏa thuận)
                    symbolInfo.setVa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("63")) {
                    // không tìm thấy field TotalTradedQttyPT để map
                    // mô tả: KLGD thỏa thuận
                    continue;
                }
                if (tag.equals("64")) {
                    // không tìm thấy field TotalTradedValuePT để map
                    // mô tả: GTGD thỏa thuận
                    continue;
                }
                if (tag.equals("65")) {
                    if (StringUtils.isBlank(value) || StringUtils.trim(value).equals("0")) {
                        symbolInfo.setIe(null);
                    } else {
                        symbolInfo.setIe(true);
                    }
                /*
                Trạng thái thực hiện quyền.
                  - 0: Bình thường
                  - 1: Trả CT bằng tiền
                  - 2: Trả cổ tức bằng CP/CP thưởng
                  - 8: Trả cổ tức bằng tiền, trả cổ tức bằng CP/CP thưởng
                  - 3: Phát hành CP cho cổ đông hiện hữu
                  - 4: Trả cổ tức bằng CP/CP thưởng,phát hành CP cho cổ đông hiện hữu
                  - 5: Trả cổ tức bằng tiền, bằng CP/CP thưởng, phát hành CP cho cổ đông hiện hữu
                  - 9: Trả cổ tức bằng tiền, phát hành CP cho cổ đông hiện hữu
                  - 6: Niêm yết bổ sung
                  - 7: Giảm vốn
                  - 10: Thay đổi tỷ lệ Free Float
                  - 11: Họp đại cổ đông
                  - 15: Chia tách cổ phiếu
                 */
                    continue;
                }
                if (tag.equals("84")) {
                    symbolInfo.setCwt(value);
                    continue;
                }
                if (tag.equals("85")) {
                    symbolInfo.setFtd(value);
                    continue;
                }
                if (tag.equals("86")) {
                    symbolInfo.setLtd(value);
                    continue;
                }
                if (tag.equals("87")) {
                    symbolInfo.setMd(value);
                    continue;
                }
                if (tag.equals("88")) {
                    symbolInfo.setFv(parseToDouble(value));
                    continue;
                }

                if (tag.equals("66")) {
                    symbolInfo.setOdC(parseToDouble(value));
                    continue;
                }
                if (tag.equals("67")) {
                    symbolInfo.setOdCh(parseToDouble(value));
                    continue;
                }
                if (tag.equals("68")) {
                    symbolInfo.setOdR(FtlNumberUtil.round4Decimal(parseToDouble(value) / 100));
                    continue;
                }
                if (tag.equals("69")) {
                    symbolInfo.setOdMv(parseToLong(value));
                    continue;
                }
                if (tag.equals("70")) {
                    symbolInfo.setOdVo(parseToDouble(value));
                    continue;
                }
                if (tag.equals("71")) {
                    symbolInfo.setOdVa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("72")) {
                    bestBidOd3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("73")) {
                    bestBidOd3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("74")) {
                    bestBidOd2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("75")) {
                    bestBidOd2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("76")) {
                    bestBidOd1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("77")) {
                    bestBidOd1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("78")) {
                    bestAskOd3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("79")) {
                    bestAskOd3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("80")) {
                    bestAskOd2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("81")) {
                    bestAskOd2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("82")) {
                    bestAskOd1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("83")) {
                    bestAskOd1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("89")) {
                    symbolInfo.setBe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("37")) {
                    symbolInfo.setIs(value);
                    continue;
                }
                if (tag.equals("90")) {
                    symbolInfo.setIsd(value);
                }
            }
            bo.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            bb.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            symbolInfo.setBo(bo);
            symbolInfo.setBb(bb);

            boOd.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            bbOd.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            symbolInfo.setBoOd(boOd);
            symbolInfo.setBbOd(bbOd);

            return symbolInfo;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static SymbolInfo parseMsg5(String symbolInfoStr) { // init index
        try {

            SymbolInfo symbolInfo = new SymbolInfo();
            symbolInfo.setT(SymbolTypeEnum.INDEX);
            IndexCount indexCount = new IndexCount();
            symbolInfo.setIc(indexCount);
            String[] fields = symbolInfoStr.split("\\|");
            symbolInfo.setS(fields[0]);
            symbolInfo.setN1(fields[0]);
            symbolInfo.setN2(fields[0]);

            if ("HNXINDEX".equalsIgnoreCase(symbolInfo.getS())) {
                symbolInfo.setN1("HNX");
                symbolInfo.setN2("HNX");
            }
            if ("HNXUPCOMINDEX".equalsIgnoreCase(symbolInfo.getS())) {
                symbolInfo.setN1("UPCOM");
                symbolInfo.setN2("UPCOM");
            }

            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("2")) {
                    if (!value.equals("0") && !value.equals("4")) {
                        return null;
                    }
                }
                if (tag.equals("3")) {
                    symbolInfo.setRe(parseToDouble(value));
                    continue;
                }
                if (tag.equals("4")) {
                    symbolInfo.setC(parseToDouble(value));
                    continue;
                }
                if (tag.equals("5")) {
                    symbolInfo.setCh(parseToDouble(value));
                    continue;
                }
                if (tag.equals("6")) {
                    symbolInfo.setR(FtlNumberUtil.round4Decimal(parseToDouble(value) / 100));
                    continue;
                }
                if (tag.equals("7")) {
                    symbolInfo.setH(parseToDouble(value));
                    continue;
                }
                if (tag.equals("8")) {
                    symbolInfo.setL(parseToDouble(value));
                    continue;
                }
//                if (tag.equals("9")) {
//                    symbolInfo.setC(parseToDouble(value));
//                    continue;
//                }
                if (tag.equals("10")) {
//                    symbolInfo.setVo(parseToDouble(value));
                    continue;
                }
                if (tag.equals("11")) {
//                    symbolInfo.setVa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("12")) {
                    symbolInfo.setVo(parseToDouble(value)); // Chỉ hiển thị KLGD khớp lệnh #20799
                    continue;
                }
                if (tag.equals("13")) {
                    symbolInfo.setVa(parseToDouble(value));  // Chỉ hiển thị GTGD khớp lệnh #20799
                    continue;
                }
                if (tag.equals("17")) {
                    indexCount.setUp(Integer.parseInt(value));
                    continue;
                }
                if (tag.equals("18")) {
                    indexCount.setCe(Integer.parseInt(value));
                    continue;
                }
                if (tag.equals("19")) {
                    indexCount.setUc(Integer.parseInt(value));
                    continue;
                }
                if (tag.equals("20")) {
                    indexCount.setDw(Integer.parseInt(value));
                    continue;
                }
                if (tag.equals("21")) {
                    indexCount.setFl(Integer.parseInt(value));
                    continue;
                }
                if (tag.equals("22")) {
                    if ("01".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("03".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.INTERMISSION.name());
                    } else if ("10".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATO.name());
                    } else if ("30".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.LO.name());
                    } else if ("40".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.ATC.name());
                    } else if ("60".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PLO.name());
                    } else if ("61".equals(value)) {
                        symbolInfo.setSs(MarketStatusEnum.PUT_THROUGH.name());
                    } else {
                        symbolInfo.setSs("CLOSED");
                    }
                    continue;
                }
                if (tag.equals("38")) {
                    symbolInfo.setO(parseToDouble(value));
                    continue;
                }
                if (tag.equals("39")) {
                    symbolInfo.setM(MarketEnum.HOSE);
                    if ("STX".equals(value)) {
                        symbolInfo.setM(MarketEnum.HNX);
                    } else if ("UPX".equals(value)) {
                        symbolInfo.setM(MarketEnum.UPCOM);
                    }
                }
            }
            // do core tra ve re = 0 nen cho nay phai tam thoi tu tinh
            if (symbolInfo.getRe() <= 0) {
                symbolInfo.setRe(symbolInfo.getC() - symbolInfo.getCh());
            }
            return symbolInfo;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static MarketStatus parseMsg6(String marketStatusStr) { // thong tin thay doi thi truong (chi lay market status)
        try {
            MarketStatus marketStatus = new MarketStatus();
            String[] fields = marketStatusStr.split("\\|");
            String market = fields[0];
            marketStatus.setType(MarketStatusTypeEnum.EQUITY);
            if ("STX".equals(market)) {
                marketStatus.setMarket(MarketEnum.HNX);
            } else if ("UPX".equals(market)) {
                marketStatus.setMarket(MarketEnum.UPCOM);
            } else if ("STO".equals(market)) {
                marketStatus.setMarket(MarketEnum.HOSE);
            } else {
                return null; // ignore BOND, DERIVATIVES
            }
            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("4")) {
                    if ("01".equals(value)) { // docs core ghi là mở cửa nhưng Tuan NA giải thích đó là cờ đánh dấu ngày mới chứ vẫn đang closed
                        marketStatus.setStatus(MarketStatusEnum.CLOSED);
                    } else if ("03".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.INTERMISSION);
                    } else if ("10".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.ATO);
                    } else if ("30".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.LO);
                    } else if ("40".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.ATC);
                    } else if ("60".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.PLO);
                    } else if ("61".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.PUT_THROUGH);
                    } else {
                        marketStatus.setStatus(MarketStatusEnum.CLOSED);
                    }
                }
            }
            if (marketStatus.getStatus() == null) {
                return null; // only get status info -> ignore other info
            }
            return marketStatus;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static MarketStatus parseMsg7(String marketStatusStr) { // init market (chi lay market status)
        try {
            MarketStatus marketStatus = new MarketStatus();
            String[] fields = marketStatusStr.split("\\|");
            String market = fields[0];
            marketStatus.setType(MarketStatusTypeEnum.EQUITY);
            if ("STX".equals(market)) {
                marketStatus.setMarket(MarketEnum.HNX);
            } else if ("UPX".equals(market)) {
                marketStatus.setMarket(MarketEnum.UPCOM);
            } else if ("STO".equals(market)) {
                marketStatus.setMarket(MarketEnum.HOSE);
            } else if ("DVX".equals(market)) {
                marketStatus.setMarket(MarketEnum.HNX);
                marketStatus.setType(MarketStatusTypeEnum.DERIVATIVES);
            } else {
                return null; // ignore BOND
            }
            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("4")) {
                    if ("01".equals(value)) { // docs core ghi là mở cửa nhưng Tuan NA giải thích đó là cờ đánh dấu ngày mới chứ vẫn đang closed
                        marketStatus.setStatus(MarketStatusEnum.CLOSED);
                    } else if ("03".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.INTERMISSION);
                    } else if ("10".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.ATO);
                    } else if ("30".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.LO);
                    } else if ("40".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.ATC);
                    } else if ("60".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.PLO);
                    } else if ("61".equals(value)) {
                        marketStatus.setStatus(MarketStatusEnum.PUT_THROUGH);
                    } else {
                        marketStatus.setStatus(MarketStatusEnum.CLOSED);
                    }
                }
            }
            return marketStatus;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static Object parseMsg3(String indexQuoteStr) { // indexQuote
        try {
            Quote quote = new Quote();
            IndexCount indexCount = new IndexCount();
            quote.setT(SymbolTypeEnum.INDEX);

            String[] fields = indexQuoteStr.split("\\|");
            quote.setS(fields[0]);
            boolean isQuote = true;
            boolean isContainIc = false;

            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("2")) {
                    if (!value.equals("0") && !value.equals("4")) {
                        isQuote = false;
                    }
                }
                if (tag.equals("4")) {
                    quote.setC(parseToDouble(value));
                    continue;
                }
                if (tag.equals("5")) {
                    quote.setCh(parseToDouble(value));
                    continue;
                }
                if (tag.equals("6")) {
                    quote.setR(FtlNumberUtil.round4Decimal(parseToDouble(value) / 100));
                    continue;
                }
                if (tag.equals("7")) {
                    quote.setH(parseToDouble(value));
                    continue;
                }
                if (tag.equals("8")) {
                    quote.setL(parseToDouble(value));
                    continue;
                }
                if (tag.equals("10")) {
                    quote.setVo(parseToDouble(value));
                    continue;
                }
                if (tag.equals("11")) {
                    quote.setVa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("12")) {
                    quote.setVonm(parseToDouble(value));
                    continue;
                }
                if (tag.equals("13")) {
                    quote.setVanm(parseToDouble(value));
                    continue;
                }
                if (tag.equals("17")) {
                    indexCount.setUp(Integer.parseInt(value));
                    isContainIc = true;
                    continue;
                }
                if (tag.equals("18")) {
                    indexCount.setCe(Integer.parseInt(value));
                    isContainIc = true;
                    continue;
                }
                if (tag.equals("19")) {
                    indexCount.setUc(Integer.parseInt(value));
                    isContainIc = true;
                    continue;
                }
                if (tag.equals("20")) {
                    indexCount.setDw(Integer.parseInt(value));
                    isContainIc = true;
                    continue;
                }
                if (tag.equals("21")) {
                    indexCount.setFl(Integer.parseInt(value));
                    isContainIc = true;
                    continue;
                }
                if (tag.equals("22")) {
                    if ("01".equals(value)) {
                        quote.setSs(MarketStatusEnum.ATO.name());
                        isQuote = false;
                    } else if ("03".equals(value)) {
                        quote.setSs(MarketStatusEnum.INTERMISSION.name());
                        isQuote = false;
                    } else if ("10".equals(value)) {
                        quote.setSs(MarketStatusEnum.ATO.name());
                        isQuote = false;
                    } else if ("30".equals(value)) {
                        quote.setSs(MarketStatusEnum.LO.name());
                    } else if ("40".equals(value)) {
                        quote.setSs(MarketStatusEnum.ATC.name());
                        isQuote = false;
                    } else if ("60".equals(value)) {
                        quote.setSs(MarketStatusEnum.PLO.name());
                        isQuote = false;
                    } else if ("61".equals(value)) {
                        quote.setSs(MarketStatusEnum.PUT_THROUGH.name());
                        isQuote = false;
                    } else {
                        quote.setSs("CLOSED");
                        isQuote = false;
                    }
                }
                if (tag.equals("38")) {
                    quote.setO(parseToDouble(value));
                }
                if (tag.equals("42")) {
                    try {
                        Date date = FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmssSSS").parse(value);
                        quote.setTi(FtlDateUtils.dateToTimestamp(date));
                    } catch (Exception ex) {
                        log.error("error while parse ti of msgType 3", ex);
                        quote.setTi(FtlDateUtils.dateToTimestamp(new Date()));
                    }
                    continue;
                }
                if (tag.equals("43")) {
                    quote.setMv(parseToDouble(value));
                }
            }
            if ((quote.getMv() != null && quote.getMv() <= 0)
                    || (quote.getC() != null && quote.getC() <= 0)
                    || (quote.getVo() != null && quote.getVo() <= 0)
                    || (quote.getVa() != null && quote.getVa() <= 0)
            ) {
                isQuote = false;
            }
            if (isContainIc) {
                quote.setIc(indexCount);
            }
            if (isQuote) {
                return quote;
            } else {
                ExtraQuote extraQuote = new ExtraQuote();
                BeanUtil.copyProperties(quote, extraQuote);
                return extraQuote;
            }
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static BidOffer parseMsg8(String bidOfferStr) { // bidOffer
        try {
            BidOffer bidOffer = new BidOffer();
            String[] fields = bidOfferStr.split("\\|");
            bidOffer.setS(fields[0]);
            List<BidOfferItem> bb = new ArrayList<>(); // bid
            List<BidOfferItem> bo = new ArrayList<>(); // ask

            BidOfferItem bestBid1 = new BidOfferItem();
            BidOfferItem bestBid2 = new BidOfferItem();
            BidOfferItem bestBid3 = new BidOfferItem();
            BidOfferItem bestAsk1 = new BidOfferItem();
            BidOfferItem bestAsk2 = new BidOfferItem();
            BidOfferItem bestAsk3 = new BidOfferItem();

            bb.add(bestBid1);
            bb.add(bestBid2);
            bb.add(bestBid3);

            bo.add(bestAsk1);
            bo.add(bestAsk2);
            bo.add(bestAsk3);

            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 1) {
                    continue;
                }
                String value = null;
                if (properties.length >= 2) {
                    value = properties[1];
                }
                String tag = properties[0];
                if (tag.equals("1")) {
                    bestBid3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("2")) {
                    bestBid3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("3")) {
                    bestBid2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("4")) {
                    bestBid2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("5")) {
                    bestBid1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("6")) {
                    bestBid1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("7")) {
                    bestAsk3.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("8")) {
                    bestAsk3.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("9")) {
                    bestAsk2.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("10")) {
                    bestAsk2.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("11")) {
                    bestAsk1.setP(parseToDouble(value));
                    continue;
                }
                if (tag.equals("12")) {
                    bestAsk1.setV(parseToLong(value));
                    continue;
                }
                if (tag.equals("13")) {
                    bidOffer.setTbo(parseToLong(value));
                    continue;
                }
                if (tag.equals("14")) {
                    bidOffer.setToo(parseToLong(value));
                    continue;
                }
                if (tag.equals("15")) {
                    Date date = FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmssSSS").parse(value);
                    bidOffer.setTi(FtlDateUtils.dateToTimestamp(date));
                    continue;
                }
                if (tag.equals("16")) {
                    if ("01".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.ATO.name());
                    } else if ("03".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.INTERMISSION.name());
                    } else if ("10".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.ATO.name());
                    } else if ("30".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.LO.name());
                    } else if ("40".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.ATC.name());
                    } else if ("60".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.PLO.name());
                    } else if ("61".equals(value)) {
                        bidOffer.setSs(MarketStatusEnum.PUT_THROUGH.name());
                    } else {
                        bidOffer.setSs("CLOSED");
                    }
                /*Trạng thái phiên giao dịch.
                      - 01: Mở cửa
                      - 03: Tạm ngưng
                      - 10: ATO
                      - 30: Liên tục
                      - 40: ATC
                      - 60: PLO
                      - 61: Thỏa thuận
                      - 90: Kết thúc nhận lệnh
                      - 96: Kết thúc ngày
                */
                    continue;
                }
                if (tag.equals("17")) {
                    bidOffer.setOe(StringUtils.trim(value));
                }
            }
            bo.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            bb.removeIf(bidOfferItem -> bidOfferItem.getP() <= 0 && bidOfferItem.getV() <= 0);
            bidOffer.setBo(bo);
            bidOffer.setBb(bb);

            return bidOffer;
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }


    public static Object parseMsg9(String stockQuoteStr) { // stockQuote or expected price
        try {
            Quote quote = new Quote();

            String[] fields = stockQuoteStr.split("\\|");
            quote.setS(fields[0]);
            String type = "A";  // A: tam khop, M: khop, O: lo le
            Date date = new Date();
            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("1")) {
                    date = FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmssSSS").parse(value);
                    quote.setTi(FtlDateUtils.dateToTimestamp(date));
                    log.info("time: {}", value);
                    continue;
                }
                if (tag.equals("3")) {
                    quote.setC(parseToDouble(value));
                    continue;
                }
                if (tag.equals("4")) {
                    quote.setCh(parseToDouble(value));
                    continue;
                }
                if (tag.equals("5")) {
                    quote.setR(FtlNumberUtil.round4Decimal(parseToDouble(value) / 100));
                    continue;
                }
                if (tag.equals("6")) {
                    quote.setMv(parseToDouble(value));
                    continue;
                }
                if (tag.equals("7")) {
                    if ("S".equalsIgnoreCase(value)) {
                        quote.setMb(SellBuyTypeEnum.SELL.name());
                    } else if ("B".equalsIgnoreCase(value)) {
                        quote.setMb(SellBuyTypeEnum.BUY.name());
                    } else {
                        quote.setMb("BS");
                    }
                }
                if (tag.equals("8")) {
                    quote.setVo(parseToDouble(value));
                    quote.setVonm(parseToDouble(value));
                    continue;
                }
                if (tag.equals("9")) {
                    quote.setVa(parseToDouble(value));
                    quote.setVanm(parseToDouble(value));
                    continue;
                }
                if (tag.equals("10")) {
                    quote.setL(parseToDouble(value));
                    continue;
                }
                if (tag.equals("11")) {
                    quote.setH(parseToDouble(value));
                    continue;
                }
                if (tag.equals("12")) {
                    quote.setFrBvo(parseToLong(value));
                    continue;
                }
                if (tag.equals("13")) {
                    quote.setFrBva(parseToDouble(value));
                    continue;
                }
                if (tag.equals("14")) {
                    quote.setFrSvo(parseToLong(value));
                    continue;
                }
                if (tag.equals("15")) {
                    quote.setFrSva(parseToDouble(value));
                    continue;
                }
                if (tag.equals("16")) {
                    quote.setFrCr(parseToLong(value));
                    continue;
                }
                if (tag.equals("17")) {
                    quote.setFrTr(parseToLong(value));
                    continue;
                }
                if (tag.equals("18")) {
                    quote.setBa(parseToDouble(value));
                    continue;
                }
                if (tag.equals("19")) {
                    type = value;
                    continue;
                }
                if (tag.equals("20")) {
                    quote.setO(parseToDouble(value));
                    continue;
                }
                if (tag.equals("22")) {
                    quote.setBe(parseToDouble(value));
                }
            }
            if (type.equalsIgnoreCase("M")) {
                quote.setSs("LO");
                quote.setA(quote.getVa() / quote.getVo());
                return quote;
            } else if (type.equalsIgnoreCase("A")) {
                ExtraQuote extraQuote = new ExtraQuote();
                extraQuote.setS(quote.getS());
                extraQuote.setEp(quote.getC());
                extraQuote.setEv(quote.getMv());
                extraQuote.setEr(quote.getR());
                extraQuote.setEc(quote.getCh());
                extraQuote.setTi(quote.getTi());
                extraQuote.setSs("ATO");

                String timeStr = FtlDateUtils.TIME_FORMAT().format(date);
                if (timeStr.compareTo("120000") > 0) {
                    extraQuote.setSs("ATC");
                }
                return extraQuote;
            } else {
                QuoteOddLot quoteOddLot = new QuoteOddLot();
                quoteOddLot.setS(quote.getS());
                quoteOddLot.setTi(quote.getTi());
                quoteOddLot.setDt(quote.getDt());
                quoteOddLot.setOdC(quote.getC());
                quoteOddLot.setOdCh(quote.getCh());
                quoteOddLot.setOdR(quote.getR());
                quoteOddLot.setOdMv(quote.getMv());
                quoteOddLot.setOdVo(quote.getVo());
                quoteOddLot.setOdVa(quote.getVa());
                quoteOddLot.setMb(quote.getMb());
                return quoteOddLot;
            }
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    public static Object parseMsg10(String msgStr) { // deal notice or advertised
        try {
            String[] fields = msgStr.split("\\|");
            String symbol = fields[0];
            String type = "A";  // A: tam khop, M: khop
            Date date = new Date();

            long ti = 0; // timestamp
            MarketEnum m = null; // Market Type
            double ptmp = 0; // match price
            double ptmvo = 0; // match volume
            double ptmva = 0; // match value
            double ptvo = 0; // pt trading volume (acumulate trading volume)
            double ptva = 0; // pt trading value (acumulate trading value)
            SellBuyTypeEnum sb = null; // sellBuyType
            double p = 0; // price
            double v = 0; // volume


            for (int i = 1; i < fields.length; i++) {
                String field = fields[i];
                String[] properties = field.split("\\*");
                if (properties.length < 2) {
                    continue;
                }
                String tag = properties[0];
                String value = properties[1];
                if (tag.equals("1")) {
                    date = FtlDateUtils.DATE_FORMAT("yyyyMMddHHmmssSSS").parse(value);
                    ti = FtlDateUtils.dateToTimestamp(date);
                    log.info("time: {}", value);
                    continue;
                }
                if (tag.equals("3")) {
                    p = parseToDouble(value);
                    ptmp = parseToDouble(value);
                    continue;
                }
                if (tag.equals("4")) {
                    v = parseToDouble(value);
                    ptmvo = parseToDouble(value);
                    continue;
                }
//                if (tag.equals("6")) {
//                    ptmvo = parseToDouble(value);
//                    continue;
//                }
                if (tag.equals("7")) {
                    if ("S".equalsIgnoreCase(value)) {
                        sb = SellBuyTypeEnum.SELL;
                    } else if ("B".equalsIgnoreCase(value)) {
                        sb = SellBuyTypeEnum.BUY;
                    }
                }
                if (tag.equals("9")) {
                    // todo
                    continue;
                }
                if (tag.equals("10")) {
                    type = StringUtils.trim(value);
                    continue;
                }
                if (tag.equals("12")) {
                    ptvo = parseToLong(value);
                    continue;
                }
                if (tag.equals("13")) {
                    ptva = parseToDouble(value);
                    continue;
                }
                if (tag.equals("14")) {
                    if ("STX".equals(value)) {
                        m = MarketEnum.HNX;
                    } else if ("UPX".equals(value)) {
                        m = MarketEnum.UPCOM;
                    } else if ("STO".equals(value)) {
                        m = MarketEnum.HOSE;
                    } else if ("DVX".equals(value)) {
                        m = MarketEnum.HNX;
                    } else if ("HCX".equals(value)) {
                        m = MarketEnum.HNX;
                    }
                }
            }
            if (type.equalsIgnoreCase("M")) {
                DealNotice dealNotice = new DealNotice();
                dealNotice.setS(symbol);
                dealNotice.setTi(ti);
                dealNotice.setM(m);
                dealNotice.setPtmp(ptmp);
                dealNotice.setPtmvo(ptmvo);
                dealNotice.setPtmva(ptmvo * ptmp);
                dealNotice.setPtvo(ptvo);
                dealNotice.setPtva(ptva);
                return dealNotice;
            } else {
                Advertised advertised = new Advertised();
                advertised.setS(symbol);
                advertised.setTi(ti);
                advertised.setSb(sb);
                advertised.setM(m);
                advertised.setP(p);
                advertised.setV(v);
                return advertised;
            }
        } catch (Exception ex) {
            log.error("", ex);
            return null;
        }
    }

    private static double parseToDoubleThenMul(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value) * 1000;
    }

    private static double parseToDouble(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    private static long parseToLongThenMul(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value) * 1000;
    }

    private static long parseToLong(String value) {
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value);
    }
}
