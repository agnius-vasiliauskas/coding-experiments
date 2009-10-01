(**
F# script for evaluating math expressions. Not uses abstract syntax tree (or F# quotes), 
nor it uses fsyacc/fslex tools for token parsing. Expression parsing is done in code itself.
However there are some "limitations" - functions can only be called with tupled parameters 
(curried style is not allowed). Another drawback is that it is kinda slow. It`s obvious - 
eval is heavily based on lists manipulation - spliting/joining lists periodically. 
So - use at your own risk !!
**)

#light
#nowarn "20"

open System.Diagnostics;

/// Unary operators
let unaryop = 
    let unaryoplst:(string*(float->float))list = 
        [("abs",abs);("acos",acos);("asin",asin);
        ("atan",atan);("cos",cos);("cosh",cosh);("exp",exp);("log",log);
        ("log10",log10);("sign",(fun x -> float (sign x)));("sin",sin);
        ("sinh",sinh);("sqrt",sqrt);("tan",tan);("tanh",tanh)]
    unaryoplst |> Map.of_list

/// Binary operators
let binaryop =
    let binaryoplst:(string*(float->float->float))list =
        [("**",( ** ));("*",(*));("/",(/));("+",(+));("-",(-))]
    binaryoplst |> Map.of_list

/// Group character list into expression list
let rec exprlst strfrom strto =
    let prefixoperator (x,y) = (x,y) = ("(","-")
    let gluechars (x,y) = 
             (x,y)=("*","*") || (x,y) = ("g","1")  ||
             (int x.[0] >= 97 && int x.[0] <= 122 && int y.[0] >= 97 && int y.[0] <= 122 )  ||
             let xnum,ynum = int x.[0] >= 48 && int x.[0] <= 57 , int y.[0] >= 48 && int y.[0] <= 57 in
             let xpt,ypt = x=".",y="." in
             (xnum,ynum) = (true,true) || (xnum,ypt) = (true,true) || (xpt,ynum) = (true,true)
    match strfrom,strto with
    | hf::tf,[] -> exprlst tf [hf]
    | hf::tf,(ht:string)::tt when gluechars (ht.[ht.Length-1].ToString(),hf) -> exprlst tf ([ht^hf]@tt)
    | hf::tf,ht::tt when prefixoperator (ht,hf) -> exprlst (["0"]@strfrom) (strto)
    | hf::tf,strto -> exprlst tf ([hf]@strto)
    | _ -> strto |> List.rev

/// Return sub-expression from expression around given operator
let subexpr ind (elst:(string)list) = 
    let optype =
        match () with
        | _ when unaryop.ContainsKey elst.[ind] -> "unary"
        | _ when binaryop.ContainsKey elst.[ind] -> "binary"
        | _ -> failwith ("Expecting operator, but given '"^elst.[ind]^"'") 
    let bracketssum tsind = 
        let subl = (Array.of_list elst).[(min ind tsind)..(max ind tsind)]
        let charcount ch (arr:string[]) = arr |> Array.fold (fun a x -> if x = ch then a+1 else a) 0
        abs ((charcount "(" subl) - (charcount ")" subl))
    let rec operandsind l r =
        let l,r = max l 0, min r (elst.Length-1)
        let lb,rb = bracketssum l,bracketssum r
        match (lb,rb) with
        | 0,0 when unaryop.ContainsKey elst.[r] -> operandsind l (r+1)
        | 0,0 -> (if elst.[l] = "(" && l > 0 && unaryop.ContainsKey elst.[l-1] then l-1 else l), r
        | _,0 -> operandsind (l-1) r
        | 0,_ -> operandsind l (r+1)
        | _ -> operandsind (l-1) (r+1)
    let lop,rop = operandsind (if optype = "unary" then ind else ind-1) (ind+1)
    let arr = Array.of_list elst in 
    List.of_array arr.[..lop-1], List.of_array arr.[lop..rop], List.of_array arr.[rop+1..]

/// Evaluate expression list and return aggregated value
let rec evallst exlst = 
    let opforeval ex =
        let binopind (op:string) = Map([("**",1);("*",2);("/",2);("+",3);("-",3)]).[op]
        let comparebyprec (x:int*string) (y:int*string) =
            let (i1,op1), (i2,op2) = (x,y)
            let comp = 
                match () with
                | _ when (op1.Length > 2 && op2.Length > 2) -> compare i2 i1
                | _ when (op1.Length > 2) && (not (op2.Length > 2)) -> -1
                | _ when (not (op1.Length > 2)) && (op2.Length > 2) -> 1
                | _ -> let rez1 = binopind op1 in let rez2 = binopind op2 in 
                       if rez1 <> rez2 then compare rez1 rez2 else compare i1 i2
            comp
        ex |> List.mapi(fun i x -> (i,x)) |> 
        List.filter (fun (i,c) -> unaryop.ContainsKey c || binaryop.ContainsKey c) |>
        List.sortWith (fun x y -> comparebyprec x y)
    let operators = opforeval exlst
    match operators with
    | [] -> exlst
    | [x] when unaryop.ContainsKey (snd x) -> [string(exlst.[2] |> float |> unaryop.[snd x])]
    | [x] when binaryop.ContainsKey (snd x) -> [string(let fil = exlst |> List.filter(fun x -> x<>"(" && x<>")") in 
                                                    (binaryop.[snd x]) (fil.[0] |> float) (fil.[2] |> float))]
    | h1::h2::t -> let el,em,er = subexpr (fst h1) exlst in
                   let el,em,er = if (el=[] && er=[]) then subexpr (fst h2) exlst else el,em,er in 
                   evallst (el@(evallst em)@er)
    | _ -> failwith "Error in eval"

/// Wrapper to function evallst,- final function which should be called by user
let eval (expr:string) = 
  (expr.Replace(" ","") |> Seq.to_list |> List.map (fun x -> x.ToString()) |> exprlst) [] |> evallst |> List.hd |> float

/// Expressions to evaluate for test purposes. Can be changed as required.
let testEval = 
    [
    ("5+4",   fun _ -> 5.+4.);
    ("3*5+14/7",   fun _ -> 3.*5.+14./7.);
    ("tanh(96)+4.45/7.8",   fun _ -> tanh(96.)+4.45/7.8);
    ("6+sqrt(7.12)/(1-exp(6.6))",   fun _ -> 6.+sqrt(7.12)/(1.-exp(6.6)));
    ("14*cos(5)+sin(14)/9",   fun _ -> 14.*cos(5.)+sin(14.)/9.);
    ("sqrt(5**log(47))",   fun _ -> sqrt(5.**log(47.)));
    ("5-tanh(2+9)-4*8/(16*sin(5))",   fun _ -> 5.-tanh(2.+9.)-4.*8./(16.*sin(5.)));
    ("cosh(14*exp(2.6))-15/(log10(4**(8+sin(9*60))))",   fun _ -> cosh(14.*exp(2.6))-15./(log10(4.**(8.+sin(9.*60.)))))
    ]
printfn "%A" "-----------------------------------------"
printfn "%A" "--- Testing eval function correctness ---"
printfn "%A" "-----------------------------------------"

testEval |> List.iteri (fun i (expr,res)->
                (i+1).ToString()^". "^(abs(1.-(eval expr)/(res ()))<1e-8).ToString()^" ==> "^ expr^" = "^(res ()).ToString() |> 
                 printfn "%A")

printfn "%A" "-----------------------------------------"
printfn "%A" "---    Testing eval function speed    ---"
printfn "%A" "-----------------------------------------"

testEval |> List.iteri (fun i (expr,res)->
                        let sw1, sw2 = Stopwatch(), Stopwatch() in
                        sw1.Start()
                        let ev = [1..100] |> List.map (fun x -> eval expr)
                        sw1.Stop()
                        sw2.Start()
                        let ca = [1..100] |> List.map (fun x -> res ())
                        sw2.Stop()
                        let dif = sw1.ElapsedTicks/sw2.ElapsedTicks in
                        printfn "%A" ((i+1).ToString()^". Eval slower than plain F# calculation by "^dif.ToString()^"x")
                      )

System.Console.ReadKey()