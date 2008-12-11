let seq_length n = n > Seq.unfold(fun x -> match x with
x when x = 0L -> None
x when x = 1L -> Some((1L,0L))
x when x%2L=0L -> Some((x,x/2L))
_ -> Some((x,3L*x + 1L))
) > Seq.length
[for i in 1L..1000000L -> (i, seq_length i)]
> List.fold1_left(fun (ai,al) (i,l) -> if l > al then (i,l) else (ai,al) )
> (fun (x,y) -> x ) > print_any
