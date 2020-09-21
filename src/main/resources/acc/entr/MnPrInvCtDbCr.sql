select 13 as SRTY, MNFPRC.DAT, 'INVENTORY' as ACDB, ITMCT.NME as SADNM, ITMCT.IID as SADID, 1004 as SADTY, SUM(TOTU) as DEBT,
'INVENTORY' as ACCR, ITMCTU.NME as SACNM, ITMCTU.IID as SACID, 1004 as SACTY, SUM(TOTU) as CRED
from MNPMCS
join (select DRID, ITM as ITMU, sum(TOT) as TOTU from DRITENR where RVID is null and DRTY=2007 group by DRID, ITMU) as DRITENR on DRITENR.DRID=MNPMCS.IID
join ITM as ITMU on ITMU.IID=DRITENR.ITMU
join ITMCT as ITMCTU on ITMCTU.IID=ITMU.CAT
join MNFPRC on MNFPRC.IID=MNPMCS.OWNR
join ITM on ITM.IID=MNFPRC.ITM
join ITMCT on ITMCT.IID=ITM.CAT
where MNPMCS.RVID is null :WHEAD
group by SRTY, MNFPRC.DAT, ACDB, SADNM, SADID, SADTY, ACCR, SACNM, SACID, SACTY
