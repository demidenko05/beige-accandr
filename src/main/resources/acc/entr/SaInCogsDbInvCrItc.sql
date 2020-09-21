select 6 as SRTY, DAT, 'COGS' as ACDB, ITMCT.NME as SADNM, ITMCT.IID as SADID, 1004 as SADTY, SUM(COGSENR.TOT) as DEBT,
'INVENTORY' as ACCR, ITMCT.NME as SACNM, ITMCT.IID as SACID, 1004 as SACTY, SUM(COGSENR.TOT) as CRED
from (select DOWID, ITM, TOT from COGSENR where RVID is null and DOWTY=6) as COGSENR
join SALINV on SALINV.IID=COGSENR.DOWID
join ITM on ITM.IID=COGSENR.ITM
join ITMCT on ITMCT.IID=ITM.CAT
where SALINV.RVID is null :WHEAD
group by SRTY, DAT, ACDB, SADNM, SADID, SADTY, ACCR, SACNM, SACID, SACTY
